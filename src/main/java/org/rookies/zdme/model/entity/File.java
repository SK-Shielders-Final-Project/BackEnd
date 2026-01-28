package org.rookies.zdme.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "ext", length = 10)
    private String ext;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
