package mx.edu.uteq.backend.service;

import java.util.List;

import mx.edu.uteq.backend.dto.BookingRequestDTO;
import mx.edu.uteq.backend.dto.BookingResponseDTO;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO requestDTO);

    BookingResponseDTO getBookingById(Long id);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO updateBooking(Long id, BookingRequestDTO requestDTO);

    void deleteBooking(Long id);

    // Búsqueda avanzada por fechas (dd-MM-yyyy), propiedad, estado y usuario
    List<BookingResponseDTO> searchBookings(String startDate, String endDate, Long propertyId, String status, Long userId);

}
