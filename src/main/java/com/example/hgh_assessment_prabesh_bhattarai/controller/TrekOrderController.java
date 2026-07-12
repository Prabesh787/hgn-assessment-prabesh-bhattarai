package com.example.hgh_assessment_prabesh_bhattarai.controller;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekOrderRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekkerRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.ApiResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.TrekOrderDetailResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.TrekOrderResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.TrekkerResponse;
import com.example.hgh_assessment_prabesh_bhattarai.service.TrekOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class TrekOrderController {

    private final TrekOrderService orderService;

    public TrekOrderController(TrekOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrekOrderResponse>> create(@Valid @RequestBody CreateTrekOrderRequest request) {
        TrekOrderResponse body = TrekOrderResponse.from(orderService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Order created", body));
    }

    /** The group: the order with its full trekker list. */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<TrekOrderDetailResponse>> get(@PathVariable Long orderId) {
        TrekOrderDetailResponse body = TrekOrderDetailResponse.from(orderService.get(orderId));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Order retrieved", body));
    }

    @PostMapping("/{orderId}/trekkers")
    public ResponseEntity<ApiResponse<TrekkerResponse>> addTrekker(@PathVariable Long orderId,
                                                                   @Valid @RequestBody CreateTrekkerRequest request) {
        TrekkerResponse body = TrekkerResponse.from(orderService.addTrekker(orderId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Trekker added", body));
    }
}
