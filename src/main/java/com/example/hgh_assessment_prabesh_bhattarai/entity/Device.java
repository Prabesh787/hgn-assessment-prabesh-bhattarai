package com.example.hgh_assessment_prabesh_bhattarai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

/**
 * A physical tracking unit. Deliberately holds no reference to an order or a
 * trekker -- that link lives in {@link DeviceAssignment} and is time-bound.
 */
@Entity
@Table(name = "device")
@Getter
@Setter
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial", nullable = false, unique = true)
    private String deviceSerial;

    /** Populated by the database default; read back after insert. */
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
