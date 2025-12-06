package mx.edu.uteq.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import mx.edu.uteq.backend.dto.BookingRequestDTO;
import mx.edu.uteq.backend.dto.BookingResponseDTO;
import mx.edu.uteq.backend.dto.UserDTO;
import mx.edu.uteq.backend.model.Booking;
import mx.edu.uteq.backend.model.Property;
import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.repository.BookingRepository;
import mx.edu.uteq.backend.repository.PropertyRepository;
import mx.edu.uteq.backend.repository.UserRepository;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    // ----- MÉTODOS CRUD -----

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO) {
        // 1. Buscar las entidades relacionadas
        Property property = propertyRepository.findById(requestDTO.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Property not found with id: " + requestDTO.getPropertyId()));

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + requestDTO.getUserId()));


        // 3. Crear la entidad Booking
        Booking booking = new Booking();
        booking.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : "PENDING"); // Valor por defecto
        booking.setStartDate(requestDTO.getStartDate());
        booking.setEndDate(requestDTO.getEndDate());
        booking.setProperty(property);
        booking.setUser(user);

        // 4. Guardar en la BD
        Booking savedBooking = bookingRepository.save(booking);

        // 5. Mapear a DTO de respuesta y devolver
        return convertToResponseDTO(savedBooking);
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = findBookingById(id);
        return convertToResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO updateBooking(Long id, BookingRequestDTO requestDTO) {

        Booking existingBooking = findBookingById(id);

        existingBooking.setStatus(requestDTO.getStatus());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        return convertToResponseDTO(updatedBooking);
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking = findBookingById(id); // Asegurarse de que existe
        bookingRepository.delete(booking);
    }

    @Override
    public List<BookingResponseDTO> searchBookings(String startDateStr, String endDateStr, Long propertyId, String status, Long userId) {
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
        try {
            if (startDateStr != null && !startDateStr.isBlank()) {
                startDate = fmt.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isBlank()) {
                endDate = fmt.parse(endDateStr);
            }
        } catch (ParseException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid date format. Use dd-MM-yyyy");
        }

        final Date s = startDate;
        final Date e = endDate;

        return bookingRepository.findAll()
                .stream()
                .filter(b -> {
                    // startDate filter: booking.startDate >= s
                    if (s != null) {
                        if (b.getStartDate() == null || b.getStartDate().before(s)) return false;
                    }
                    // endDate filter: booking.endDate <= e
                    if (e != null) {
                        if (b.getEndDate() == null || b.getEndDate().after(e)) return false;
                    }
                    if (propertyId != null) {
                        if (b.getProperty() == null || !propertyId.equals(b.getProperty().getId())) return false;
                    }
                    if (status != null && !status.isBlank()) {
                        if (b.getStatus() == null || !status.equalsIgnoreCase(b.getStatus())) return false;
                    }
                    if (userId != null) {
                        if (b.getUser() == null || !userId.equals(b.getUser().getId())) return false;
                    }
                    return true;
                })
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ----- MÉTODOS PRIVADOS DE AYUDA -----

    private BookingResponseDTO convertToResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setPropertyId(booking.getProperty().getId());
        
        if (booking.getUser() != null) {
            UserDTO uDto = new UserDTO();
            uDto.setId(booking.getUser().getId());
            uDto.setEmail(booking.getUser().getEmail());
            uDto.setRole(booking.getUser().getRole());
            dto.setUser(uDto);
            uDto.setUserProfile(booking.getUser().getUserProfile());
        }
        

        return dto;
    }

    // Método reutilizable para encontrar o fallar
    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + id));
    }
}