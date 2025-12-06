package mx.edu.uteq.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import mx.edu.uteq.backend.repository.UserRepository;
import mx.edu.uteq.backend.model.User;

import java.util.Collections;
import java.util.Set;

@Service 
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        Set<GrantedAuthority> authorities = Collections.singleton(
            new SimpleGrantedAuthority(user.getRole().toUpperCase())
        );

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),          
            user.getPassword(),       
            authorities              
        );
    }
}
