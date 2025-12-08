package mx.edu.uteq.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.edu.uteq.backend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
}