package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(

        @NotBlank(message = "deviceSerial is required")
        @Size(max = 64)
        String deviceSerial
) {
}
