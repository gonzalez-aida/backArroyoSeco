package mx.edu.uteq.backend.service;

import mx.edu.uteq.backend.model.User; 
import mx.edu.uteq.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority; 

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections; 
import java.util.List;
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
        return buildToken(
            authentication.getName(), 
            authentication.getAuthorities(), 
            authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User ? 
                ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername() : 
                authentication.getName()
        );
    }

    public String generateTokenForUser(User user) {
        Collection<? extends GrantedAuthority> authorities = createAuthoritiesFromRole(user.getRole());
        
        return buildToken(user.getEmail(), authorities, user.getEmail());
    }

    private Collection<? extends GrantedAuthority> createAuthoritiesFromRole(String role) {
        if (role != null && !role.isBlank()) {
            return Collections.singletonList(new SimpleGrantedAuthority(role.toUpperCase()));
        }
        return Collections.emptyList();
    }


    private String buildToken(String username, Collection<? extends GrantedAuthority> authorities, String subject) {
        Instant now = Instant.now();
        
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .issuer("alojando-backend")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.MINUTES)) 
            .subject(subject)
            .claim("authorities", authorities.stream()
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