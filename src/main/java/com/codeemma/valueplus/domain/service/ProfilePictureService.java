package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.persistence.entity.ProfilePicture;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.ProfilePictureRepository;
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
