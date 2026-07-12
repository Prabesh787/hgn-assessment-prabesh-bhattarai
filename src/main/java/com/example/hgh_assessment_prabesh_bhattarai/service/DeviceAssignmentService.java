package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CloseAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;

import java.util.List;

public interface DeviceAssignmentService {

    DeviceAssignment assign(Long deviceId, CreateAssignmentRequest request);

    /** Release a device without handing it to another order -- the trek finished or was cancelled. */
    DeviceAssignment close(Long deviceId, CloseAssignmentRequest request);

    List<DeviceAssignment> history(Long deviceId);
}
