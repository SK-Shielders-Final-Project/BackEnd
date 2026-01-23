package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
