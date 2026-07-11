package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findTopByDeviceIdOrderByLastSignalAtDesc(Long deviceId);

    List<Alert> findByStatusAndRaisedAtLessThanEqual(AlertStatus status, Instant cutoff);

    default List<Alert> findEscalationCandidates(Instant cutoff) {
        return findByStatusAndRaisedAtLessThanEqual(AlertStatus.OPEN, cutoff);
    }

    List<Alert> findByStatusOrderByRaisedAtDesc(AlertStatus status);

    List<Alert> findByDeviceIdOrderByRaisedAtDesc(Long deviceId);
}
