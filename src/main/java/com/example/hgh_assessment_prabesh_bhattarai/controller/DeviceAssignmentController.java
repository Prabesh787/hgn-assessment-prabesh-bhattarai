package com.example.hgh_assessment_prabesh_bhattarai.controller;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CloseAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.ApiResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.DeviceAssignmentResponse;
import com.example.hgh_assessment_prabesh_bhattarai.service.DeviceAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/devices/{deviceId}/assignments")
public class DeviceAssignmentController {

    private final DeviceAssignmentService assignmentService;

    public DeviceAssignmentController(DeviceAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceAssignmentResponse>> assign(@PathVariable Long deviceId,
                                                                        @Valid @RequestBody CreateAssignmentRequest request) {
        DeviceAssignmentResponse body =
                DeviceAssignmentResponse.from(assignmentService.assign(deviceId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Device assigned to order", body));
    }

    /** Release the device -- trek finished or cancelled. Leaves the device unassigned. */
    @PostMapping("/close")
    public ResponseEntity<ApiResponse<DeviceAssignmentResponse>> close(@PathVariable Long deviceId,
                                                                       @Valid @RequestBody CloseAssignmentRequest request) {
        DeviceAssignmentResponse body =
                DeviceAssignmentResponse.from(assignmentService.close(deviceId, request));
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Device assignment closed", body));
    }

    /** It shows the history if device assigned to many orders over time. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceAssignmentResponse>>> history(@PathVariable Long deviceId) {
        List<DeviceAssignmentResponse> body = assignmentService.history(deviceId).stream()
                .map(DeviceAssignmentResponse::from)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Assignment history retrieved", body));
    }
}
