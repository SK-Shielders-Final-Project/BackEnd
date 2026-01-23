package org.rookies.zdme.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bikes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bike_id")
    private Long bikeId;

    @Column(name = "serial_number", length = 100, unique = true)
    private String serialNumber;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "status", length = 20)
    private String status; // "AVAILABLE" / "IN_USE" / "REPAIRING"

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
