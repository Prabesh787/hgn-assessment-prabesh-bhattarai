package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotNull;

public record ClaimAlertRequest(

        @NotNull(message = "coordinatorId is required")
        Long coordinatorId
) {
}
