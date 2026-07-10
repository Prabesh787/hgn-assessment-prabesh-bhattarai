package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTrekkerRequest(

        @NotBlank(message = "fullName is required")
        @Size(max = 200)
        String fullName,

        @Size(max = 32)
        String phone
) {
}
