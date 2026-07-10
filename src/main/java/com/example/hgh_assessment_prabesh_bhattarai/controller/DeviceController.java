package com.example.hgh_assessment_prabesh_bhattarai.controller;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateDeviceRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.ApiResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.DeviceResponse;
import com.example.hgh_assessment_prabesh_bhattarai.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceResponse>> register(@Valid @RequestBody CreateDeviceRequest request) {
        DeviceResponse body = DeviceResponse.from(deviceService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Device registered", body));
    }
}
