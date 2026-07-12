package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateCoordinatorRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;

import java.util.List;

public interface CoordinatorService {

    Coordinator register(CreateCoordinatorRequest request);

    List<Coordinator> list();
}
