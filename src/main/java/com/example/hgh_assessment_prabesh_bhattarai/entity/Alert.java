package com.example.hgh_assessment_prabesh_bhattarai.entity;

import com.example.hgh_assessment_prabesh_bhattarai.enums.AlertStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Entity
@Table(name = "alert")
@Getter
@Setter
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private TrekOrder trekOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;

    @Column(name = "last_signal_at", nullable = false)
    private Instant lastSignalAt;

    @Column(name = "signal_count", nullable = false)
    private int signalCount = 1;

    @Column(name = "retransmission_count", nullable = false)
    private int retransmissionCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by_id")
    private Coordinator claimedBy;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
