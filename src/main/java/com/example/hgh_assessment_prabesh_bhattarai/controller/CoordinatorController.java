package com.example.hgh_assessment_prabesh_bhattarai.controller;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateCoordinatorRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.ApiResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.CoordinatorResponse;
import com.example.hgh_assessment_prabesh_bhattarai.service.CoordinatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coordinators")
public class CoordinatorController {

    private final CoordinatorService coordinatorService;

    public CoordinatorController(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CoordinatorResponse>> register(
            @Valid @RequestBody CreateCoordinatorRequest request) {
        CoordinatorResponse body = CoordinatorResponse.from(coordinatorService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Coordinator registered", body));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CoordinatorResponse>>> list() {
        List<CoordinatorResponse> body = coordinatorService.list().stream()
                .map(CoordinatorResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coordinators retrieved", body));
    }
}
