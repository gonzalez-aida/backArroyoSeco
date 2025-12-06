package mx.edu.uteq.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import mx.edu.uteq.backend.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
	// Comprueba si existe alguna reserva que solape con el rango para la propiedad
	boolean existsByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long propertyId, java.util.Date endDate, java.util.Date startDate);
}
