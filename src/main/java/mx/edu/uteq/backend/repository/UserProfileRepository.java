package mx.edu.uteq.backend.repository;

import mx.edu.uteq.backend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  UserProfileRepository extends JpaRepository<UserProfile, Long> {

}
