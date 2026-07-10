package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateAssignmentRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;

import java.util.List;

public interface DeviceAssignmentService {

    DeviceAssignment assign(Long deviceId, CreateAssignmentRequest request);

    List<DeviceAssignment> history(Long deviceId);
}
