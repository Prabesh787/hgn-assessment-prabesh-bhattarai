package com.example.hgh_assessment_prabesh_bhattarai;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.ClaimAlertRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import com.example.hgh_assessment_prabesh_bhattarai.repository.AlertRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.CoordinatorRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AlertClaimConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(AlertClaimConcurrencyTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @Test
    void onlyOneCoordinatorWinsTheClaim() throws Exception {
        int coordinators = 20;

        Long alertId = seedOpenAlert();
        List<Long> coordinatorIds = seedCoordinators(coordinators);

        ExecutorService pool = Executors.newFixedThreadPool(coordinators);
        CountDownLatch ready = new CountDownLatch(coordinators);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<ClaimOutcome>> results = new ArrayList<>();

        log.info("RACE: {} coordinators claiming alert {} simultaneously", coordinators, alertId);

        for (int i = 0; i < coordinators; i++) {
            Long coordinatorId = coordinatorIds.get(i);
            results.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                ResponseEntity<String> response = restTemplate.postForEntity(
                        "/alerts/" + alertId + "/claim",
                        new ClaimAlertRequest(coordinatorId),
                        String.class);
                return new ClaimOutcome(coordinatorId, response.getStatusCode().value(), response.getBody());
            }));
        }

        ready.await();      // wait until all threads are parked on the gate
        go.countDown();     // release them simultaneously

        List<ClaimOutcome> outcomes = new ArrayList<>();
        for (Future<ClaimOutcome> result : results) {
            outcomes.add(result.get());
        }
        pool.shutdown();

        for (ClaimOutcome outcome : outcomes) {
            log.info("  coordinator {} -> HTTP {} : {}",
                    outcome.coordinatorId(),
                    outcome.status(),
                    outcome.status() == 200 ? "WON THE CLAIM" : outcome.body());
        }

        long wins = outcomes.stream().filter(o -> o.status() == 200).count();
        long conflicts = outcomes.stream().filter(o -> o.status() == 409).count();

        log.info("RESULT: {} winner, {} conflicts, from {} simultaneous claims", wins, conflicts, coordinators);

        assertThat(wins).isEqualTo(1);
        assertThat(conflicts).isEqualTo(coordinators - 1);

        Alert claimed = alertRepository.findById(alertId).orElseThrow();
        assertThat(claimed.getStatus()).isEqualTo(AlertStatus.CLAIMED);
        assertThat(claimed.getClaimedAt()).isNotNull();

        log.info("DURABLE STATE: alert {} is {}, claimed at {}",
                alertId, claimed.getStatus(), claimed.getClaimedAt());
    }

    private record ClaimOutcome(Long coordinatorId, int status, String body) {
    }

    private Long seedOpenAlert() {
        Device device = new Device();
        device.setDeviceSerial("SN-CONCURRENCY-" + System.nanoTime());
        device = deviceRepository.save(device);

        Instant now = Instant.now();
        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setStatus(AlertStatus.OPEN);
        alert.setRaisedAt(now);
        alert.setLastSignalAt(now);
        alert.setSignalCount(1);
        alert.setRetransmissionCount(0);
        return alertRepository.save(alert).getId();
    }

    private List<Long> seedCoordinators(int count) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coordinator coordinator = new Coordinator();
            coordinator.setName("coordinator-" + i + "-" + System.nanoTime());
            ids.add(coordinatorRepository.save(coordinator).getId());
        }
        return ids;
    }
}
