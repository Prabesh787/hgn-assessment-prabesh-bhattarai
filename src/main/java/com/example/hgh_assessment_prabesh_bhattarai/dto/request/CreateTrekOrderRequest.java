package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTrekOrderRequest(

        @NotBlank(message = "orderRef is required")
        @Size(max = 64)
        String orderRef,

        @NotBlank(message = "trekName is required")
        @Size(max = 200)
        String trekName,

        Instant startsAt,

        Instant endsAt
) {
}
