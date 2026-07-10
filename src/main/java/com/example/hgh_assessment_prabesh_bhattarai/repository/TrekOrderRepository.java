package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrekOrderRepository extends JpaRepository<TrekOrder, Long> {

    boolean existsByOrderRef(String orderRef);
}
