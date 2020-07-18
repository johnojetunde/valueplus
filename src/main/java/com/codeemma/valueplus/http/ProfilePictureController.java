package com.codeemma.valueplus.http;

import com.codeemma.valueplus.dto.ProfilePictureDto;
import com.codeemma.valueplus.exception.NotFoundException;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.service.ProfilePictureService;
import com.codeemma.valueplus.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "v1/profile-picture", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class ProfilePictureController {

    private final ProfilePictureService profilePictureService;

    public ProfilePictureController(ProfilePictureService profilePictureService) {
        this.profilePictureService = profilePictureService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfilePictureDto createAndUpdate(@Valid @RequestBody ProfilePictureDto profilePicture) {
        User loggedInUser = UserUtils.getLoggedInUser();
        log.info("createAndUpdate() received userId = {}", loggedInUser.getId());

        return ProfilePictureDto.valueOf(
                profilePictureService.save(
                        profilePicture.toProfilePicture(loggedInUser)
                ));
    }

    @GetMapping
    public ProfilePictureDto get() {
        User loggedInUser = UserUtils.getLoggedInUser();
        log.info("get() received userId = {}", loggedInUser.getId());

        return profilePictureService.get(loggedInUser).map(ProfilePictureDto::valueOf)
                .orElseThrow(() -> new NotFoundException("entity not found"));
    }

}
