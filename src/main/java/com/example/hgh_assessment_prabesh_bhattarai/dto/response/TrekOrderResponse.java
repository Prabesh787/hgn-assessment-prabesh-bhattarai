package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;

import java.time.Instant;

public record TrekOrderResponse(
        Long id,
        String orderRef,
        String trekName,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt) {

    public static TrekOrderResponse from(TrekOrder order) {
        return new TrekOrderResponse(
                order.getId(),
                order.getOrderRef(),
                order.getTrekName(),
                order.getStartsAt(),
                order.getEndsAt(),
                order.getCreatedAt());
    }
}
