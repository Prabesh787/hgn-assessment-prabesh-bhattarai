package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.SosSignalRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertSignal;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import com.example.hgh_assessment_prabesh_bhattarai.enums.SignalKind;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;
import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.exception.NotFoundException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.AlertRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.AlertSignalRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.CoordinatorRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceAssignmentRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.TrekOrderRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.AlertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AlertServiceImpl implements AlertService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final AlertRepository alertRepository;
    private final AlertSignalRepository alertSignalRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final TrekOrderRepository orderRepository;

    private final Duration dedupWindow;

    public AlertServiceImpl(DeviceRepository deviceRepository,
                            DeviceAssignmentRepository assignmentRepository,
                            AlertRepository alertRepository,
                            AlertSignalRepository alertSignalRepository,
                            CoordinatorRepository coordinatorRepository,
                            TrekOrderRepository orderRepository,
                            @Value("${alert.dedup.window-minutes:5}") long dedupWindowMinutes) {
        this.deviceRepository = deviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.alertRepository = alertRepository;
        this.alertSignalRepository = alertSignalRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.orderRepository = orderRepository;
        this.dedupWindow = Duration.ofMinutes(dedupWindowMinutes);
    }

    @Override
    @Transactional
    public IngestResult ingest(Long deviceId, SosSignalRequest request) {

        Device device = deviceRepository.findByIdForUpdate(deviceId)
                .orElseThrow(() -> NotFoundException.device(deviceId));

        Instant receivedAt = Instant.now();
        Instant at = request.raisedAt() != null ? request.raisedAt() : receivedAt;

        Optional<Alert> latest = alertRepository.findTopByDeviceIdOrderByLastSignalAtDesc(deviceId);
        if (latest.isPresent() && isRetransmission(latest.get(), at)) {
            Alert alert = latest.get();
            alert.setSignalCount(alert.getSignalCount() + 1);
            alert.setRetransmissionCount(alert.getRetransmissionCount() + 1);
            if (at.isAfter(alert.getLastSignalAt())) {
                alert.setLastSignalAt(at);
                if (request.latitude() != null) {
                    alert.setLatitude(request.latitude());
                    alert.setLongitude(request.longitude());
                }
            }
            Alert saved = alertRepository.save(alert);
            recordSignal(saved, request, at, receivedAt, SignalKind.RETRANSMISSION);
            return new IngestResult(saved, false);
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
        alert.setRetransmissionCount(0);
        Alert saved = alertRepository.save(alert);
        recordSignal(saved, request, at, receivedAt, SignalKind.RAISED);
        return new IngestResult(saved, true);
    }

    private boolean isRetransmission(Alert alert, Instant signalAt) {
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            return false;
        }
        Duration gap = Duration.between(alert.getLastSignalAt(), signalAt).abs();
        return gap.compareTo(dedupWindow) <= 0;
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
    public Alert detail(Long alertId) {
        return alertRepository.findDetailById(alertId)
                .orElseThrow(() -> NotFoundException.alert(alertId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> history(Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw NotFoundException.device(deviceId);
        }
        return alertRepository.findByDeviceIdOrderByRaisedAtDesc(deviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertSignal> signals(Long alertId) {
        if (!alertRepository.existsById(alertId)) {
            throw NotFoundException.alert(alertId);
        }
        return alertSignalRepository.findByAlertIdOrderBySeqAsc(alertId);
    }

    private void recordSignal(Alert alert, SosSignalRequest request, Instant signaledAt,
                              Instant receivedAt, SignalKind kind) {
        AlertSignal signal = new AlertSignal();
        signal.setAlert(alert);
        signal.setLatitude(request.latitude());
        signal.setLongitude(request.longitude());
        signal.setSignaledAt(signaledAt);
        signal.setReceivedAt(receivedAt);
        signal.setSeq(alert.getSignalCount());
        signal.setKind(kind);
        alertSignalRepository.save(signal);
    }

    private TrekOrder resolveOrder(Long deviceId, Instant at) {
        List<DeviceAssignment> covering = assignmentRepository.findCoveringTimestamp(deviceId, at);
        return covering.size() == 1 ? covering.get(0).getTrekOrder() : null;
    }


    @Override
    @Transactional
    public Alert claim(Long alertId, Long coordinatorId) {
        if (!alertRepository.existsById(alertId)) {
            throw NotFoundException.alert(alertId);
        }
        Coordinator coordinator = coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> NotFoundException.coordinator(coordinatorId));

        // Atomic compare-and-swap: succeeds only if the alert is still OPEN/ESCALATED.
        int claimed = alertRepository.claim(alertId, coordinator, Instant.now());
        if (claimed == 0) {
            Alert current = alertRepository.findById(alertId)
                    .orElseThrow(() -> NotFoundException.alert(alertId));
            throw switch (current.getStatus()) {
                case RESOLVED -> new ConflictException("Alert " + alertId + " is already resolved");
                case CLAIMED -> new ConflictException(
                        "Alert " + alertId + " is already claimed by " + current.getClaimedBy().getName());
                default -> new ConflictException("Alert " + alertId + " cannot be claimed");
            };
        }
        return alertRepository.findById(alertId)
                .orElseThrow(() -> NotFoundException.alert(alertId));
    }


    @Override
    @Transactional
    public Alert assignOrder(Long alertId, Long orderId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> NotFoundException.alert(alertId));

        if (alert.getTrekOrder() != null) {
            throw new ConflictException("Alert " + alertId + " is already linked to order "
                    + alert.getTrekOrder().getId());
        }

        TrekOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> NotFoundException.order(orderId));

        alert.setTrekOrder(order);
        return alertRepository.save(alert);
    }

    @Override
    @Transactional
    public Alert resolve(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> NotFoundException.alert(alertId));

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new ConflictException("Alert " + alertId + " is already resolved");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        return alertRepository.save(alert);
    }


    @Override
    @Transactional
    public List<Alert> escalateOverdue(Duration threshold) {

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant cutoff = now.minus(threshold);

        int escalated = alertRepository.escalateOverdue(cutoff, now);
        return escalated == 0
                ? List.of()
                : alertRepository.findByStatusAndEscalatedAt(AlertStatus.ESCALATED, now);
    }

}
