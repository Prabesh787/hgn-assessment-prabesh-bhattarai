package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.SosSignalRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertStatus;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;
import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.exception.NotFoundException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.AlertRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceAssignmentRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.AlertService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AlertServiceImpl implements AlertService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final AlertRepository alertRepository;

    public AlertServiceImpl(DeviceRepository deviceRepository,
                            DeviceAssignmentRepository assignmentRepository,
                            AlertRepository alertRepository) {
        this.deviceRepository = deviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional
    public IngestResult ingest(Long deviceId, SosSignalRequest request) {

        Device device = deviceRepository.findByIdForUpdate(deviceId)
                .orElseThrow(() -> NotFoundException.device(deviceId));

        Instant at = request.raisedAt() != null ? request.raisedAt() : Instant.now();

        Optional<Alert> live = alertRepository.findLiveByDeviceId(deviceId);
        if (live.isPresent()) {
            Alert alert = live.get();
            alert.setSignalCount(alert.getSignalCount() + 1);
// Only the newest signal advances the clock and the last-known fix.
            if (at.isAfter(alert.getLastSignalAt())) {
                alert.setLastSignalAt(at);
                if (request.latitude() != null) {
                    alert.setLatitude(request.latitude());
                    alert.setLongitude(request.longitude());
                }
            }
            return new IngestResult(alertRepository.save(alert), false);
        }

        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setTrekOrder(resolveOrder(deviceId, at));
        alert.setStatus(AlertStatus.OPEN);
        alert.setLatitude(request.latitude());
        alert.setLongitude(request.longitude());
        alert.setRaisedAt(at);
        alert.setLastSignalAt(at);
        alert.setSignalCount(1);
        return new IngestResult(alertRepository.save(alert), true);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Alert> list(AlertStatus status) {
        return status != null
                ? alertRepository.findByStatusOrderByRaisedAtDesc(status)
                : alertRepository.findAll(Sort.by(Sort.Direction.DESC, "raisedAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> history(Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw NotFoundException.device(deviceId);
        }
        return alertRepository.findByDeviceIdOrderByRaisedAtDesc(deviceId);
    }

}