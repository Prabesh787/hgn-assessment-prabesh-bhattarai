package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCoordinatorRequest(

        @NotBlank(message = "name is required")
        @Size(max = 128)
        String name
) {
}
