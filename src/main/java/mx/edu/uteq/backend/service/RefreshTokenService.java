package mx.edu.uteq.backend.service;

import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private static final int REFRESH_TOKEN_LENGTH = 32;

    @Autowired
    public RefreshTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String createRefreshToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[REFRESH_TOKEN_LENGTH];
        random.nextBytes(bytes);
        
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        user.setRefreshToken(token);
        userRepository.save(user);
        
        return token;
    }

    public Optional<User> findUserByRefreshToken(String token) {
        return userRepository.findByRefreshToken(token);
    }
}
