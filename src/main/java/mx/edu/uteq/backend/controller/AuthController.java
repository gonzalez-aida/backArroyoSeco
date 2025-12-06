package mx.edu.uteq.backend.controller;

import mx.edu.uteq.backend.dto.LoginRequest;
import mx.edu.uteq.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar al usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Generar el JWT
            String token = authService.generateToken(authentication);
            
            // Extraer información del usuario
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
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
}
