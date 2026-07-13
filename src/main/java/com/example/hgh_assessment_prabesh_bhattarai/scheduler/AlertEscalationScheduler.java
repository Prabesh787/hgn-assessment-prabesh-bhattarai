package com.example.hgh_assessment_prabesh_bhattarai.scheduler;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "alert.escalation.enabled", havingValue = "true", matchIfMissing = true)
public class AlertEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertEscalationScheduler.class);

    private final AlertService alertService;
    private final Duration threshold;

    public AlertEscalationScheduler(AlertService alertService,
                                    @Value("${alert.escalation.threshold-minutes:15}") long thresholdMinutes) {
        this.alertService = alertService;
        this.threshold = Duration.ofMinutes(thresholdMinutes);
    }

    @Scheduled(fixedDelayString = "${alert.escalation.sweep-interval-ms:60000}")
    public void sweep() {
        List<Alert> escalated = alertService.escalateOverdue(threshold);
        if (escalated.isEmpty()) {
            return;
        }

        log.warn("Escalated {} unclaimed alert(s) older than {}", escalated.size(), threshold);
        Instant now = Instant.now();
        for (Alert alert : escalated) {
            log.warn("ESCALATION: alert {} unclaimed for {} minute(s) since it was raised at {}",
                    alert.getId(),
                    Duration.between(alert.getRaisedAt(), now).toMinutes(),
                    alert.getRaisedAt());
        }
    }
}
