package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;

import java.time.Instant;

public record AlertResponse(
        Long id,
        Long deviceId,
        Long orderId,
        AlertStatus status,
        Double latitude,
        Double longitude,
        Instant raisedAt,
        Instant lastSignalAt,
        int signalCount,
        int retransmissionCount,
        String claimedBy,
        Instant claimedAt,
        Instant resolvedAt,
        Instant escalatedAt) {

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getDevice().getId(),
                alert.getTrekOrder() != null ? alert.getTrekOrder().getId() : null,
                alert.getStatus(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getRaisedAt(),
                alert.getLastSignalAt(),
                alert.getSignalCount(),
                alert.getRetransmissionCount(),
                alert.getClaimedBy(),
                alert.getClaimedAt(),
                alert.getResolvedAt(),
                alert.getEscalatedAt());
    }
}
