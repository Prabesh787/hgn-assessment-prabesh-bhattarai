package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;

import java.time.Instant;
import java.util.List;

/**
 * An order together with its whole party. The order <em>is</em> the group -- a solo booking
 * is just an order with one trekker -- so no group/single flag is needed; the party size
 * says it.
 */
public record TrekOrderDetailResponse(
        Long id,
        String orderRef,
        String trekName,
        Instant startsAt,
        Instant endsAt,
        int groupSize,
        List<TrekkerResponse> trekkers) {

    public static TrekOrderDetailResponse from(TrekOrder order) {
        List<TrekkerResponse> trekkers = order.getTrekkers().stream()
                .map(TrekkerResponse::from)
                .toList();
        return new TrekOrderDetailResponse(
                order.getId(),
                order.getOrderRef(),
                order.getTrekName(),
                order.getStartsAt(),
                order.getEndsAt(),
                trekkers.size(),
                trekkers);
    }
}
