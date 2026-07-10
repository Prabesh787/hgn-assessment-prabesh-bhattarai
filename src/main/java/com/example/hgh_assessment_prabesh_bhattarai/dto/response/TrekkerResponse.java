package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Trekker;

public record TrekkerResponse(Long id, Long orderId, String fullName, String phone) {

    public static TrekkerResponse from(Trekker trekker) {
        return new TrekkerResponse(
                trekker.getId(),
                trekker.getTrekOrder().getId(),
                trekker.getFullName(),
                trekker.getPhone());
    }
}
