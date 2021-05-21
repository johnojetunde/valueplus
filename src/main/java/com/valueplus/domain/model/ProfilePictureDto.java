package com.valueplus.domain.model;

import com.valueplus.persistence.entity.ProfilePicture;
import com.valueplus.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProfilePictureDto {
    @Size(max = 1000000) //~ 1MB
    private String photo;

    public static ProfilePictureDto valueOf(ProfilePicture profilePicture) {
        return builder()
                .photo(new String(profilePicture.getPhoto()))
                .build();
    }

    public ProfilePicture toProfilePicture(User user) {
        return ProfilePicture.builder()
                .user(user)
                .photo(photo.getBytes())
                .build();
    }
}
