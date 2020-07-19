package com.codeemma.valueplus.dto;

import com.codeemma.valueplus.model.ProfilePicture;
import com.codeemma.valueplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Base64;

import static java.util.Objects.nonNull;

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
