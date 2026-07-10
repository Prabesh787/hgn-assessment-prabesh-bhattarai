package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceSerial(String deviceSerial);

    boolean existsByDeviceSerial(String deviceSerial);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Device d WHERE d.id = :id")
    Optional<Device> findByIdForUpdate(@Param("id") Long id);
}
