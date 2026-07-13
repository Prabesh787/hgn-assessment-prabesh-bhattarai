package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Alert;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;
import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findTopByDeviceIdOrderByLastSignalAtDesc(Long deviceId);

    @Query("""
            SELECT a FROM Alert a
            LEFT JOIN FETCH a.device
            LEFT JOIN FETCH a.claimedBy
            LEFT JOIN FETCH a.trekOrder o
            LEFT JOIN FETCH o.trekkers
            WHERE a.id = :alertId
            """)
    Optional<Alert> findDetailById(@Param("alertId") Long alertId);

    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Alert a
               SET a.status = :claimedStatus,
                   a.claimedBy = :coordinator,
                   a.claimedAt = :now
             WHERE a.id = :alertId
               AND a.status IN :claimable
            """)
    int claimGuarded(@Param("alertId") Long alertId,
                     @Param("coordinator") Coordinator coordinator,
                     @Param("now") Instant now,
                     @Param("claimedStatus") AlertStatus claimedStatus,
                     @Param("claimable") Collection<AlertStatus> claimable);

    /** An alert is claimable while it is still unresolved and unclaimed. */
    default int claim(Long alertId, Coordinator coordinator, Instant now) {
        return claimGuarded(alertId, coordinator, now, AlertStatus.CLAIMED,
                List.of(AlertStatus.OPEN, AlertStatus.ESCALATED));
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Alert a
               SET a.status = :escalatedStatus,
                   a.escalatedAt = :now
             WHERE a.status = :openStatus
               AND a.raisedAt <= :cutoff
            """)
    int escalateGuarded(@Param("cutoff") Instant cutoff,
                        @Param("now") Instant now,
                        @Param("escalatedStatus") AlertStatus escalatedStatus,
                        @Param("openStatus") AlertStatus openStatus);


    default int escalateOverdue(Instant cutoff, Instant now) {
        return escalateGuarded(cutoff, now, AlertStatus.ESCALATED, AlertStatus.OPEN);
    }

    List<Alert> findByStatusAndEscalatedAt(AlertStatus status, Instant escalatedAt);

    List<Alert> findByStatusOrderByRaisedAtDesc(AlertStatus status);

    List<Alert> findByDeviceIdOrderByRaisedAtDesc(Long deviceId);
}
