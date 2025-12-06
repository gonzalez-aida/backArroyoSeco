package mx.edu.uteq.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import mx.edu.uteq.backend.model.Property;
import java.util.List;


@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Optional<Property> findByName(String name);
    Optional<Property> findById(Long id);
    List<Property> findByOwnerId_Id(Long ownerId);
}