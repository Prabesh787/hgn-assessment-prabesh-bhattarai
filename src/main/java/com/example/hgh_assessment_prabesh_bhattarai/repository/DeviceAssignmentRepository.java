package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, Long> {

    /** At most one row can match: {@code ux_device_active_assignment} guarantees it. */
    @Query("SELECT a FROM DeviceAssignment a WHERE a.device.id = :deviceId AND a.active = true")
    Optional<DeviceAssignment> findActiveByDeviceId(@Param("deviceId") Long deviceId);


    @Query("""
            SELECT a FROM DeviceAssignment a
            WHERE a.device.id = :deviceId
              AND a.assignedFrom <= :at
              AND (a.assignedTo IS NULL OR a.assignedTo > :at)
            """)
    List<DeviceAssignment> findCoveringTimestamp(@Param("deviceId") Long deviceId, @Param("at") Instant at);

    List<DeviceAssignment> findByDeviceIdOrderByAssignedFromDesc(Long deviceId);
}
