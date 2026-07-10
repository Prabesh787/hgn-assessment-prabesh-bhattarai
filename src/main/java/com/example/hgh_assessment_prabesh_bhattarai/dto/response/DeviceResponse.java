package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;

import java.time.Instant;

public record DeviceResponse(Long id, String deviceSerial, Instant createdAt) {

    public static DeviceResponse from(Device device) {
        return new DeviceResponse(device.getId(), device.getDeviceSerial(), device.getCreatedAt());
    }
}
