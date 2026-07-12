package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignOrderRequest(

        @NotNull(message = "orderId is required")
        Long orderId
) {
}
