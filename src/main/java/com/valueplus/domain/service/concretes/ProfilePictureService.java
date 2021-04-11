package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.domain.model.ProfilePictureDto;
import com.valueplus.persistence.entity.ProfilePicture;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.ProfilePictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.valueplus.domain.enums.ActionType.USER_PROFILE_PICTURE_UPDATE;
import static com.valueplus.domain.enums.EntityType.USER_PROFILE_PICTURE;
import static com.valueplus.domain.util.MapperUtil.copy;

@RequiredArgsConstructor
@Service
public class ProfilePictureService {
    private final ProfilePictureRepository profilePictureRepository;
    private final AuditEventPublisher auditEvent;

    public ProfilePicture save(ProfilePicture profilePicture) {
        Optional<ProfilePicture> optional = profilePictureRepository.findByUser(profilePicture.getUser());

        ProfilePicture existing = optional.orElse(profilePicture);
        var oldObject = optional.map(p -> copy(p, ProfilePicture.class))
                .orElse(new ProfilePicture());

        existing.setPhoto(profilePicture.getPhoto());
        var savedEntity = profilePictureRepository.save(existing);

        auditEvent.publish(oldObject, savedEntity, USER_PROFILE_PICTURE_UPDATE, USER_PROFILE_PICTURE);
        return savedEntity;
    }

    public Optional<ProfilePicture> get(User user) {
        return profilePictureRepository.findByUser(user);
    }

    public Optional<String> getImage(User user) {
        return profilePictureRepository.findByUser(user)
                .map(ProfilePictureDto::valueOf)
                .map(ProfilePictureDto::getPhoto);

    }
}
