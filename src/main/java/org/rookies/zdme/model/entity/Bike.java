package org.rookies.zdme.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.model.enums.BikeStatus; // BikeStatus 임포트 추가

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

    @Column(name = "SERIAL_NUMBER", length = 100, unique = true)
    private String serialNumber;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BikeStatus status; // "AVAILABLE" / "IN_USE" / "REPAIRING"

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_photo_file_id", referencedColumnName = "file_id")
    private File returnPhoto;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
