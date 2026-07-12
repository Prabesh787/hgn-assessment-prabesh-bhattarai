package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;

import java.time.Instant;

/**
 * Everything a coordinator needs to act on one alert, including the trekking party that was
 * carrying the device. {@code order} is null when the signal could not be attributed to a
 * group -- the alert is still recorded, and needs manual triage before anyone is dispatched.
 */
public record AlertDetailResponse(
        Long id,
        Long deviceId,
        String deviceSerial,
        AlertStatus status,
        Double latitude,
        Double longitude,
        Instant raisedAt,
        Instant lastSignalAt,
        int signalCount,
        int retransmissionCount,
        Long claimedById,
        String claimedByName,
        Instant claimedAt,
        Instant resolvedAt,
        Instant escalatedAt,
        TrekOrderDetailResponse order) {

    public static AlertDetailResponse from(Alert alert) {
        return new AlertDetailResponse(
                alert.getId(),
                alert.getDevice().getId(),
                alert.getDevice().getDeviceSerial(),
                alert.getStatus(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getRaisedAt(),
                alert.getLastSignalAt(),
                alert.getSignalCount(),
                alert.getRetransmissionCount(),
                alert.getClaimedBy() != null ? alert.getClaimedBy().getId() : null,
                alert.getClaimedBy() != null ? alert.getClaimedBy().getName() : null,
                alert.getClaimedAt(),
                alert.getResolvedAt(),
                alert.getEscalatedAt(),
                alert.getTrekOrder() != null ? TrekOrderDetailResponse.from(alert.getTrekOrder()) : null);
    }
}
