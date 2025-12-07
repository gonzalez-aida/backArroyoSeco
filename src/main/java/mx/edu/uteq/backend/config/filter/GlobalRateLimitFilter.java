package mx.edu.uteq.backend.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.uteq.backend.service.RateLimitService; 
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public GlobalRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        
        if (!rateLimitService.tryConsume(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); 
            response.getWriter().write("429 Too Many Requests: Ha excedido el límite global de peticiones.");
            return; 
        }
        
        filterChain.doFilter(request, response);
    }
}