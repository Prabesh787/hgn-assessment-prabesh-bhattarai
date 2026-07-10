package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateDeviceRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;

public interface DeviceService {

    Device register(CreateDeviceRequest request);
}
