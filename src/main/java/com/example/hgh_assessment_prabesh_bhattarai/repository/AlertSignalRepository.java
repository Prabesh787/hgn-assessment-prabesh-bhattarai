package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.AlertSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertSignalRepository extends JpaRepository<AlertSignal, Long> {

    List<AlertSignal> findByAlertIdOrderBySeqAsc(Long alertId);
}
