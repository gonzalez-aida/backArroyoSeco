package mx.edu.uteq.backend.controller;

import mx.edu.uteq.backend.dto.LoginRequest;
import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.repository.UserRepository; 
import mx.edu.uteq.backend.service.AuthService;
import mx.edu.uteq.backend.service.RefreshTokenService; 
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional; 

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService; 
    private final UserRepository userRepository; 

    public AuthController(@Lazy AuthenticationManager authenticationManager, AuthService authService,
                          RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            String accessToken = authService.generateToken(authentication);
            
            Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
            if (userOpt.isEmpty()) {
                 throw new RuntimeException("Usuario autenticado no encontrado en DB.");
            }
            User user = userOpt.get();
            String refreshToken = refreshTokenService.createRefreshToken(user); 

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken); 
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            error.put("message", "El email o la contraseña son incorrectos");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en el servidor");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Intercambio de refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token es requerido."));
        }
        Optional<User> userOpt = refreshTokenService.findUserByRefreshToken(refreshToken);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token inválido o expirado."));
        }
        
        User user = userOpt.get();
        
        String newAccessToken = authService.generateTokenForUser(user); 
        
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
