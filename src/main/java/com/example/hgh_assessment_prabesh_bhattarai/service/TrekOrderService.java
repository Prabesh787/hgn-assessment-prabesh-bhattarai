package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekOrderRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekkerRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Trekker;

public interface TrekOrderService {

    TrekOrder create(CreateTrekOrderRequest request);

    Trekker addTrekker(Long orderId, CreateTrekkerRequest request);
}
