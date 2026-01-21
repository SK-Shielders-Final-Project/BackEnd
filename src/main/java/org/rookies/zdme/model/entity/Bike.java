package org.rookies.zdme.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bikes")
@Getter
@NoArgsConstructor
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bikeId;

    @Column(unique = true, length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String modelName;

    @Column(length = 20)
    private String status;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
