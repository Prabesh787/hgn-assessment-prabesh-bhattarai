package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import jakarta.validation.constraints.AssertTrue;

import java.time.Instant;

public record SosSignalRequest(

        Double latitude,

        Double longitude,

        Instant raisedAt
) {

    @AssertTrue(message = "latitude and longitude must be provided together and within valid ranges")
    public boolean isLocationValid() {
        if (latitude == null && longitude == null) {
            return true;
        }
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
