package com.codeemma.valueplus.repository;

import com.codeemma.valueplus.model.ProfilePicture;
import com.codeemma.valueplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    Optional<ProfilePicture> findByUser(User user);
}
