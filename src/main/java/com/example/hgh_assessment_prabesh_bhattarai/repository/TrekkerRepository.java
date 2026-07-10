package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.Trekker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrekkerRepository extends JpaRepository<Trekker, Long> {

    List<Trekker> findByTrekOrderId(Long orderId);
}
