package com.codeemma.valueplus.dto;

import com.codeemma.valueplus.model.ProfilePicture;
import com.codeemma.valueplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Base64;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProfilePictureDto {
    private String photo;

    public ProfilePicture toProfilePicture(User user) {
        byte[] photoByte = Base64.getDecoder().decode(photo);
        return ProfilePicture.builder()
                .user(user)
                .photo(photoByte).build();
    }

    public static ProfilePictureDto valueOf(ProfilePicture profilePicture) {
        return builder()
                .photo(Base64.getEncoder().encodeToString(profilePicture.getPhoto()))
                .build();
    }
}
