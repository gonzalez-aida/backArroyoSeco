package mx.edu.uteq.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtService {

    // Obtiene el ID del usuario directamente del claim "userId" del JWT
    public Long getCurrentUserId() {
    Jwt jwt = getCurrentJwt();
    if (jwt == null) {
        throw new RuntimeException("No se pudo obtener el JWT del contexto de seguridad");
    }
    Object userIdClaim = jwt.getClaim("userId");

    if (userIdClaim == null) {
        throw new RuntimeException("El claim 'userId' no se encontró en el token JWT, verifique AuthorizationServerConfig.");
    }
    if (userIdClaim instanceof Number) {
        return ((Number) userIdClaim).longValue();
    }
    try {
        return Long.parseLong(userIdClaim.toString());
    } catch (NumberFormatException e) {
        throw new RuntimeException("El claim 'userId' no es un número válido en el token: " + userIdClaim.toString());
    }
}

    // Obtiene el username/email del usuario actual
    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getSubject() : null;
    }

    // Obtiene los roles/authorities del usuario actual
    public List<String> getCurrentUserAuthorities() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return List.of();
        }
        return jwt.getClaimAsStringList("authorities");
    }

    // Verifica si el usuario actual tiene un rol específico. 
    public boolean hasRole(String role) {
        List<String> authorities = getCurrentUserAuthorities();
        return authorities != null && authorities.contains(role);
    }

    // Obtiene el JWT completo del contexto de seguridad.
    private Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken();
        }
        
        return null;
    }

    // Obtiene un claim específico del JWT. 
    public <T> T getClaim(String claimName, Class<T> clazz) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return null;
        }
        return jwt.getClaim(claimName);
    }
}
