package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;

import java.time.Instant;

public record CoordinatorResponse(Long id, String name, Instant createdAt) {

    public static CoordinatorResponse from(Coordinator coordinator) {
        return new CoordinatorResponse(coordinator.getId(), coordinator.getName(), coordinator.getCreatedAt());
    }
}
