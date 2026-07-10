package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.AssignmentEndReason;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;

import java.time.Instant;

public record DeviceAssignmentResponse(
        Long id,
        Long deviceId,
        Long orderId,
        Instant assignedFrom,
        Instant assignedTo,
        boolean active,
        AssignmentEndReason endReason) {

    public static DeviceAssignmentResponse from(DeviceAssignment assignment) {
        return new DeviceAssignmentResponse(
                assignment.getId(),
                assignment.getDevice().getId(),
                assignment.getTrekOrder().getId(),
                assignment.getAssignedFrom(),
                assignment.getAssignedTo(),
                assignment.isActive(),
                assignment.getEndReason());
    }
}
