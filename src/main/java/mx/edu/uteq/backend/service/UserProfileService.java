package mx.edu.uteq.backend.service;

import mx.edu.uteq.backend.model.User;
import mx.edu.uteq.backend.model.UserProfile;
import mx.edu.uteq.backend.repository.UserProfileRepository;
import mx.edu.uteq.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfile updateUserProfileByUserId(Long userId, UserProfile profileDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        UserProfile existingProfile = user.getUserProfile();

        if (existingProfile == null) {
            throw new RuntimeException("El usuario no tiene un perfil asociado para actualizar.");
        }


        existingProfile.setName(profileDetails.getName());
        existingProfile.setLastName(profileDetails.getLastName());
        existingProfile.setCellphone(profileDetails.getCellphone());
        existingProfile.setCountry(profileDetails.getCountry());

        return userProfileRepository.save(existingProfile);
    }

    public UserProfile getUserProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));


        UserProfile profile = user.getUserProfile();

        if (profile == null) {
            throw new RuntimeException("El usuario no tiene un perfil asociado.");
        }

        return profile;
    }


    public UserProfile createProfileForUser(Long userId, UserProfile profileDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (user.getUserProfile() != null) {
            throw new RuntimeException("El usuario ya tiene un perfil.");
        }

        UserProfile newProfile = userProfileRepository.save(profileDetails);
        user.setUserProfile(newProfile);
        userRepository.save(user);

        return newProfile;
    }

    public void deleteProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        UserProfile profileToDelete = user.getUserProfile();
        if (profileToDelete != null) {
            user.setUserProfile(null);
            userRepository.save(user);
            userProfileRepository.delete(profileToDelete);
        }
    }
}
