package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.ProfilePicture;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    Optional<ProfilePicture> findByUser(User user);
}
