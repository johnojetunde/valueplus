package com.codeemma.valueplus.dto;

import com.codeemma.valueplus.model.ProfilePicture;
import com.codeemma.valueplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Base64;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProfilePictureDto {
    @Size(max = 1000000) //~ 1MB
    private String photo;

    public static ProfilePictureDto valueOf(ProfilePicture profilePicture) {
        return builder()
                .photo(Base64.getEncoder().encodeToString(profilePicture.getPhoto()))
                .build();
    }

    public ProfilePicture toProfilePicture(User user) {
        byte[] photoByte = Base64.getDecoder().decode(extractBase64(photo));
        return ProfilePicture.builder()
                .user(user)
                .photo(photoByte).build();
    }

    private String extractBase64(String photo) {
        String[] split = photo.split(",");

        return split.length > 1 ? split[1] : split[0];
    }
}
