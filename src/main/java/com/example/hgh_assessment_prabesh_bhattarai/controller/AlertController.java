package com.example.hgh_assessment_prabesh_bhattarai.controller;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.ClaimAlertRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.SosSignalRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.ApiResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.AlertResponse;
import com.example.hgh_assessment_prabesh_bhattarai.dto.response.AlertSignalResponse;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import com.example.hgh_assessment_prabesh_bhattarai.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /** Take in an SOS signal from a device (deduped into its live alert). */
    @PostMapping("/devices/{deviceId}/alerts")
    public ResponseEntity<ApiResponse<AlertResponse>> ingest(@PathVariable Long deviceId,
                                                             @Valid @RequestBody SosSignalRequest request) {
        AlertService.IngestResult result = alertService.ingest(deviceId, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = result.created() ? "SOS alert raised" : "Signal folded into existing alert";
        return ResponseEntity.status(status)
                .body(ApiResponse.success(status.value(), message, AlertResponse.from(result.alert())));
    }

    /** Full alert history for a device, newest first. */
    @GetMapping("/devices/{deviceId}/alerts")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> history(@PathVariable Long deviceId) {
        List<AlertResponse> body = alertService.history(deviceId).stream()
                .map(AlertResponse::from)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Alert history retrieved", body));
    }

    /** Coordinator dashboard: all alerts, optionally filtered by status. */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> list(
            @RequestParam(required = false) AlertStatus status) {
        List<AlertResponse> body = alertService.list(status).stream()
                .map(AlertResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Alerts retrieved", body));
    }

    /** Full signal trail for one alert, in the order the signals folded in. */
    @GetMapping("/alerts/{alertId}/signals")
    public ResponseEntity<ApiResponse<List<AlertSignalResponse>>> signals(@PathVariable Long alertId) {
        List<AlertSignalResponse> body = alertService.signals(alertId).stream()
                .map(AlertSignalResponse::from)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Alert signals retrieved", body));
    }

    /** A coordinator claims an alert to respond to it. */
    @PostMapping("/alerts/{alertId}/claim")
    public ResponseEntity<ApiResponse<AlertResponse>> claim(@PathVariable Long alertId,
                                                            @Valid @RequestBody ClaimAlertRequest request) {
        AlertResponse body = AlertResponse.from(alertService.claim(alertId, request.coordinatorId()));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Alert claimed", body));
    }

    /** Close out an alert once the emergency is over. */
    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>> resolve(@PathVariable Long alertId) {
        AlertResponse body = AlertResponse.from(alertService.resolve(alertId));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Alert resolved", body));
    }
}
