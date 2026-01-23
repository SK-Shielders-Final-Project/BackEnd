package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
