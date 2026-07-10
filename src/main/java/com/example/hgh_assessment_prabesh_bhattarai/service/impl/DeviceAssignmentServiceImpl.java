package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.AssignmentEndReason;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;
import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.exception.NotFoundException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceAssignmentRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.TrekOrderRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.DeviceAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceAssignmentServiceImpl implements DeviceAssignmentService {

    private final DeviceRepository deviceRepository;
    private final TrekOrderRepository orderRepository;
    private final DeviceAssignmentRepository assignmentRepository;

    public DeviceAssignmentServiceImpl(DeviceRepository deviceRepository,
                                       TrekOrderRepository orderRepository,
                                       DeviceAssignmentRepository assignmentRepository) {
        this.deviceRepository = deviceRepository;
        this.orderRepository = orderRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional
    public DeviceAssignment assign(Long deviceId, CreateAssignmentRequest request) {
        Device device = deviceRepository.findByIdForUpdate(deviceId)
                .orElseThrow(() -> NotFoundException.device(deviceId));

        TrekOrder order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> NotFoundException.order(request.orderId()));

        Instant assignedFrom = request.assignedFrom() != null ? request.assignedFrom() : Instant.now();

        Optional<DeviceAssignment> prior = assignmentRepository.findActiveByDeviceId(deviceId);
        if (prior.isPresent()) {
            DeviceAssignment current = prior.get();

            if (current.getTrekOrder().getId().equals(order.getId())) {
                throw new ConflictException(
                        "Device " + deviceId + " is already assigned to order " + order.getId());
            }
            if (!assignedFrom.isAfter(current.getAssignedFrom())) {
                throw new ConflictException(
                        "assignedFrom must be after the current assignment started at "
                                + current.getAssignedFrom());
            }

            current.setAssignedTo(assignedFrom);
            current.setActive(false);
            current.setEndReason(AssignmentEndReason.REASSIGNED);

            assignmentRepository.saveAndFlush(current);
        }

        DeviceAssignment assignment = new DeviceAssignment();
        assignment.setDevice(device);
        assignment.setTrekOrder(order);
        assignment.setAssignedFrom(assignedFrom);
        assignment.setActive(true);
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceAssignment> history(Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw NotFoundException.device(deviceId);
        }
        return assignmentRepository.findByDeviceIdOrderByAssignedFromDesc(deviceId);
    }
}
