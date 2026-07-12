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

    /**
     * One alert with everything a coordinator needs to act on it -- device, claiming
     * coordinator, and the whole trekking party -- in a single query. A shared device cannot
     * say which trekker pressed it, so the group is the unit of response: this is what makes
     * an alert from a shared device make sense at the group level.
     */
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

    List<Alert> findByStatusAndRaisedAtLessThanEqual(AlertStatus status, Instant cutoff);

    default List<Alert> findEscalationCandidates(Instant cutoff) {
        return findByStatusAndRaisedAtLessThanEqual(AlertStatus.OPEN, cutoff);
    }

    List<Alert> findByStatusOrderByRaisedAtDesc(AlertStatus status);

    List<Alert> findByDeviceIdOrderByRaisedAtDesc(Long deviceId);
}
