package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.NotFoundException;
import com.codeemma.valueplus.domain.dto.PasswordChange;
import com.codeemma.valueplus.domain.dto.ProfilePictureDto;
import com.codeemma.valueplus.domain.dto.UserDto;
import com.codeemma.valueplus.domain.dto.UserUpdate;
import com.codeemma.valueplus.domain.service.concretes.PasswordService;
import com.codeemma.valueplus.domain.service.concretes.ProfilePictureService;
import com.codeemma.valueplus.domain.service.concretes.UserService;
import com.codeemma.valueplus.domain.util.UserUtils;
import com.codeemma.valueplus.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;
    private final PasswordService passwordService;
    private final ProfilePictureService profilePictureService;

    public UserController(UserService userService,
                          PasswordService passwordService,
                          ProfilePictureService profilePictureService
    ) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.profilePictureService = profilePictureService;
    }

    @GetMapping
    public Page<UserDto> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                 @RequestParam(name = "size", defaultValue = "50") Integer size) {
        log.debug("findAll() pageNumber ={}, size = {}", pageNumber, size);
        return userService.findUsers(PageRequest.of(pageNumber, size)).map(UserDto::valueOf);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable("userId") long userId) {
        log.debug("getUser() id = {}", userId);
        return userService.find(userId).map(UserDto::valueOf).orElseThrow(() -> new NotFoundException("user not found"));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(method = RequestMethod.GET, path = "/current")
    public UserDto getCurrentUser() {
        User user = UserUtils.getLoggedInUser();
        String photo = profilePictureService.get(user)
                .map(ProfilePictureDto::valueOf)
                .map(ProfilePictureDto::getPhoto)
                .orElse(null);
        return UserDto.valueOf(user, photo);
    }

    @Deprecated //todo: remove when not in use
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(method = RequestMethod.GET, path = "/current/details")
    public UserDto getLoggedInUser() {
        User user = UserUtils.getLoggedInUser();
        String photo = profilePictureService.get(user)
                .map(ProfilePictureDto::valueOf)
                .map(ProfilePictureDto::getPhoto)
                .orElse(null);
        return UserDto.valueOf(user, photo);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{userId}")
    public UserDto update(@RequestParam("userId") long userId, @Valid @RequestBody UserUpdate userUpdate) {
        return UserDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/current")
    public UserDto update(@Valid @RequestBody UserUpdate userUpdate) {
        long userId = UserUtils.getLoggedInUser().getId();
        return UserDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<?> delete(@PathVariable("userId") Long userId) throws Exception {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/current/password-change")
    public UserDto update(@RequestBody PasswordChange passwordChange) {
        long userId = UserUtils.getLoggedInUser().getId();
        return UserDto.valueOf(
                passwordService.changePassword(userId, passwordChange)
        );
    }

}
