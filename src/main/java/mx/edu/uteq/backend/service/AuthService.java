package mx.edu.uteq.backend.service;

import mx.edu.uteq.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;

    public AuthService(UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Instant now = Instant.now();
        
        // Construir los claims del JWT
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .issuer("alojando-backend")
            .issuedAt(now)
            .expiresAt(now.plus(30, ChronoUnit.MINUTES))
            .subject(username)
            .claim("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        userRepository.findByEmail(username).ifPresent(user -> {
            claimsBuilder.claim("userId", user.getId());
            claimsBuilder.claim("email", user.getEmail());
        });
        
        JwtClaimsSet claims = claimsBuilder.build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}