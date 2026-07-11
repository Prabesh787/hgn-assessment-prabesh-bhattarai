package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClaimAlertRequest(

        @NotBlank(message = "coordinator is required")
        String coordinator
) {
}
