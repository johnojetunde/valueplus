package com.codeemma.valueplus.service;

import com.codeemma.valueplus.model.ProfilePicture;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.ProfilePictureRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfilePictureService {
    private final ProfilePictureRepository profilePictureRepository;

    public ProfilePictureService(ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = profilePictureRepository;
    }

    public ProfilePicture save(ProfilePicture profilePicture) {
        Optional<ProfilePicture> optional = profilePictureRepository.findByUser(profilePicture.getUser());
        if (!optional.isPresent()) {
            return profilePictureRepository.save(profilePicture);
        }

        ProfilePicture existing = optional.get();
        existing.setPhoto(profilePicture.getPhoto());
        return profilePictureRepository.save(existing);
    }

    public Optional<ProfilePicture> get(User user) {
        return profilePictureRepository.findByUser(user);
    }
}
