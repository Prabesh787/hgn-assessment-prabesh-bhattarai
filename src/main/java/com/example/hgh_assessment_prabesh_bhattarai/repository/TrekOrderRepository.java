package com.example.hgh_assessment_prabesh_bhattarai.repository;

import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrekOrderRepository extends JpaRepository<TrekOrder, Long> {

    boolean existsByOrderRef(String orderRef);

    /** Order with its whole party in one query -- the group is the point, so never lazy-load it. */
    @Query("""
            SELECT o FROM TrekOrder o
            LEFT JOIN FETCH o.trekkers
            WHERE o.id = :orderId
            """)
    Optional<TrekOrder> findDetailById(@Param("orderId") Long orderId);
}
