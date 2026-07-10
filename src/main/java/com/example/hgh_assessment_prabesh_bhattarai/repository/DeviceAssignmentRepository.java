package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, Long> {


    List<DeviceAssignment> findByDeviceIdOrderByAssignedFromDesc(Long deviceId);
}
