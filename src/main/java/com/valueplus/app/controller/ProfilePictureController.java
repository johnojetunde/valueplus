package com.valueplus.app.controller;

import com.valueplus.domain.model.ProfilePictureDto;
import com.valueplus.domain.service.concretes.ProfilePictureService;
import com.valueplus.domain.util.ProfilePictureUtils;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(path = "v1/profile-picture", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Api
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
    @ApiOperation(value = "get profile picture", notes = "return a default picture")
    public ProfilePictureDto get() throws IOException {
        User loggedInUser = UserUtils.getLoggedInUser();
        log.info("get() received userId = {}", loggedInUser.getId());

        return profilePictureService.get(loggedInUser).map(ProfilePictureDto::valueOf)
                .orElse(new ProfilePictureDto(ProfilePictureUtils.defaultImageBase64()));
    }

}
