package mx.edu.uteq.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mx.edu.uteq.backend.dto.BookingRequestDTO;
import mx.edu.uteq.backend.dto.BookingResponseDTO;
import mx.edu.uteq.backend.service.BookingService;
import mx.edu.uteq.backend.service.JwtService;

@RestController
@RequestMapping("/api/bookings") // Ruta base para todas las operaciones de bookings
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtService jwtService;

    //GET avanzado busqueda por fechas, propiedad, estatus y usuario
    // ahora acepta opcionalmente userId como query param; si no viene, intenta obtenerlo del JWT; si no existe, pasa null
    @GetMapping("/search")
    public ResponseEntity<List<BookingResponseDTO>> searchBookings(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        Long userIdForSearch = null;

        if (userId != null) {
            // usar userId proporcionada en la solicitud
            userIdForSearch = userId;
        } else {
            // intentar obtener userId desde el token; si falla o no existe, queda null
            try {
                Long currentUserId = jwtService.getCurrentUserId();
                if (currentUserId != null) {
                    userIdForSearch = currentUserId;
                }
            } catch (Exception e) {
            }
        }

        List<BookingResponseDTO> bookings = bookingService.searchBookings(startDate, endDate, propertyId, status, userIdForSearch);
        return ResponseEntity.ok(bookings);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody BookingRequestDTO requestDTO) {
        // asignar userId desde el JWT antes de crear la reserva
        requestDTO.setUserId(jwtService.getCurrentUserId());
        BookingResponseDTO newBooking = bookingService.createBooking(requestDTO);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED); 
    }

    // READ (By ID)
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        BookingResponseDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    // READ (All)
    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings); 
    }

    // UPDATE solo cambia el estado
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> updateBooking(@PathVariable Long id, @RequestBody BookingRequestDTO requestDTO) {
        BookingResponseDTO updatedBooking = bookingService.updateBooking(id, requestDTO);
        return ResponseEntity.ok(updatedBooking);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        //mensaje de respuesta con texto "borrado correctamente"
        return ResponseEntity.ok("Borrado correctamente");
    }

    // Nuevo endpoint: obtener todas las reservaciones del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
        Long currentUserId = jwtService.getCurrentUserId();
        List<BookingResponseDTO> bookings = bookingService.searchBookings(null, null, null, null, currentUserId);
        return ResponseEntity.ok(bookings);
    }
}