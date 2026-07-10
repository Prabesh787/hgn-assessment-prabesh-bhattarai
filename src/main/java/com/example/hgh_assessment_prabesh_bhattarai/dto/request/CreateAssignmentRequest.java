package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateAssignmentRequest(

        @NotNull(message = "orderId is required")
        Long orderId,

        /**
         * When the device starts serving this order. Optional; defaults to now.
         * Also the instant at which the prior assignment is closed, so the two
         * windows meet exactly and leave no gap.
         */
        Instant assignedFrom
) {
}
