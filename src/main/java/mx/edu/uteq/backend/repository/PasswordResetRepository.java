package mx.edu.uteq.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.edu.uteq.backend.model.PasswordReset;
import mx.edu.uteq.backend.model.User;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}
