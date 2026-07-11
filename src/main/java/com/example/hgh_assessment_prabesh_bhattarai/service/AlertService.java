package com.example.hgh_assessment_prabesh_bhattarai.service;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.SosSignalRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertStatus;

import java.time.Duration;
import java.util.List;

public interface AlertService {

    IngestResult ingest(Long deviceId, SosSignalRequest request);

    List<Alert> list(AlertStatus status);

    List<Alert> history(Long deviceId);

}
