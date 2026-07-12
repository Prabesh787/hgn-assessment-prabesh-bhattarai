package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.SosSignalRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertSignal;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;

import java.util.List;

public interface AlertService {

    IngestResult ingest(Long deviceId, SosSignalRequest request);

    List<Alert> list(AlertStatus status);

    List<Alert> history(Long deviceId);

    List<AlertSignal> signals(Long alertId);

    record IngestResult(Alert alert, boolean created) {
    }

    Alert claim(Long alertId, Long coordinatorId);

    /** Manual triage for an alert the system could not attribute to an order. */
    Alert assignOrder(Long alertId, Long orderId);

    Alert resolve(Long alertId);



}
