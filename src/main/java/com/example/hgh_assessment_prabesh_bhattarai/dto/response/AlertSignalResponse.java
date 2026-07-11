package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertSignal;
import com.example.hgh_assessment_prabesh_bhattarai.enums.SignalKind;

import java.time.Instant;

public record AlertSignalResponse(
        Long id,
        Long alertId,
        Double latitude,
        Double longitude,
        Instant signaledAt,
        Instant receivedAt,
        int seq,
        SignalKind kind) {

    public static AlertSignalResponse from(AlertSignal signal) {
        return new AlertSignalResponse(
                signal.getId(),
                signal.getAlert().getId(),
                signal.getLatitude(),
                signal.getLongitude(),
                signal.getSignaledAt(),
                signal.getReceivedAt(),
                signal.getSeq(),
                signal.getKind());
    }
}
