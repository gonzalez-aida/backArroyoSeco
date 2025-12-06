package mx.edu.uteq.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.edu.uteq.backend.dto.RegisterRequestDTO;
import mx.edu.uteq.backend.model.PasswordReset;
import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.model.UserProfile;
import mx.edu.uteq.backend.repository.PasswordResetRepository;
import mx.edu.uteq.backend.repository.UserProfileRepository;
import mx.edu.uteq.backend.repository.UserRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordResetRepository resetRepository;

    @Autowired
    private JavaMailSender mailSender;

    /* public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return user;
    } */

    @Transactional
    public void registerUser(RegisterRequestDTO request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new IllegalArgumentException("El correo electrónico ya está en uso.");
    }

    UserProfile profile = new UserProfile();
    profile.setName(request.getName());
    profile.setLastName(request.getLastName());
    profile.setCellphone(request.getCellphone());
    profile.setCountry(request.getCountry());
    userProfileRepository.save(profile);

    User newUser = new User();
    newUser.setEmail(request.getEmail());
    newUser.setPassword(passwordEncoder.encode(request.getPassword())); 
    newUser.setRole(request.getRole() != null ? request.getRole() : "visitante");
    newUser.setCreationDate(new Date());
    newUser.setLogDate(null);
    newUser.setUserProfile(profile); 

    userRepository.save(newUser);
}

    // -------------------------------- Restablecer contraseña -------------------------------------------
    @Transactional
    public String sendResetCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("El email no está registrado");
        }
        User user = optionalUser.get();

        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        sendEmail(email, code);

        PasswordReset resetCode = new PasswordReset(user, code);
        resetRepository.save(resetCode);
        return "Código de verificación enviado al correo.";
    }

    private void sendEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("arroyosecoreservas@gmail.com");
        message.setTo(email);
        message.setSubject("Código de verificación para restablecer contraseña");
        message.setText("Tu código de verificación es: " + code + "\n\nUsa este código para poder restaurar tu contraseña.");
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
        throw new RuntimeException("Usuario no encontrado");
    }
    User user = userOpt.get();

    Optional<PasswordReset> resetOpt = resetRepository
            .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user);

    if (resetOpt.isEmpty()) {
        throw new RuntimeException("No hay código de verificación pendiente para este usuario");
    }
    PasswordReset reset = resetOpt.get();

    return reset.getCode().equals(code);
}

    @Transactional
    public String resetPassword(String email, String code, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
        throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();

        Optional<PasswordReset> resets = resetRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user);
    if (resets.isEmpty()) {
        throw new RuntimeException("No hay código de verificación pendiente para este usuario");
    }

    PasswordReset lastReset = resets.get();

    if (!lastReset.getCode().equals(code)) {
        throw new RuntimeException("Código inválido");
    }

    String encryptedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(encryptedPassword);

    userRepository.save(user);

    lastReset.setUsed(true);
    resetRepository.save(lastReset);

    return "Contraseña actualizada exitosamente";
}

// --------------------------------------------------------------------------------------------------------------------
   
@Transactional
public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    userRepository.delete(user);
}

}
