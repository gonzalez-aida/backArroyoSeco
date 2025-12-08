package mx.edu.uteq.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional; 

import mx.edu.uteq.backend.dto.BookingRequestDTO;
import mx.edu.uteq.backend.dto.BookingResponseDTO;
import mx.edu.uteq.backend.dto.UserDTO;
import mx.edu.uteq.backend.model.Booking;
import mx.edu.uteq.backend.model.Property;
import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.repository.BookingRepository;
import mx.edu.uteq.backend.repository.PropertyRepository;
import mx.edu.uteq.backend.repository.UserRepository;
import mx.edu.uteq.backend.util.BookingStatusConstants; 
import mx.edu.uteq.backend.util.DateConstants; 

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    
    @Override
    @Transactional 
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO) {

        Date startDate = requestDTO.getStartDate();
        Date endDate = requestDTO.getEndDate();

        if (startDate == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de inicio es requerida."
            );
        }

        if (endDate == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de fin es requerida."
            );
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        if (!startDate.before(endDate)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de inicio debe ser estrictamente anterior a la fecha de fin."
            );
        }

        // No se permite crear reservas en el pasado
        if (startDate.before(today)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No se pueden crear reservas con fecha de inicio en el pasado."
            );
        }

        Long propertyId = requestDTO.getPropertyId();
        
        Property property = propertyRepository.findById(propertyId) 
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property not found with id: " + propertyId
            ));

        boolean overlaps = bookingRepository
            .existsByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                propertyId, endDate, startDate
            );

        if (overlaps) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La propiedad ya está reservada en el rango de fechas solicitado."
            );
        }

        User user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found with id: " + requestDTO.getUserId()
            ));

        // crear booking
        Booking booking = new Booking();
        booking.setStatus(
            requestDTO.getStatus() != null ?
            requestDTO.getStatus() :
            BookingStatusConstants.PENDING
        );
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setProperty(property); 
        booking.setUser(user);

        Booking saved = bookingRepository.save(booking);

        return convertToResponseDTO(saved);
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
        Booking booking = findBookingById(id);
        bookingRepository.delete(booking);
    }

    @Override
    public List<BookingResponseDTO> searchBookings(String startDateStr, String endDateStr, Long propertyId, String status, Long userId) {
        Date startDate = null;
        Date endDate = null;
        
        SimpleDateFormat fmt = new SimpleDateFormat(DateConstants.DATE_FORMAT);
        fmt.setLenient(false);
        
        try {
            if (startDateStr != null && !startDateStr.isBlank()) {
                startDate = fmt.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isBlank()) {
                endDate = fmt.parse(endDateStr);
            }
        } catch (ParseException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid date format. Use " + DateConstants.DATE_FORMAT
            );
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

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + id));
    }
}