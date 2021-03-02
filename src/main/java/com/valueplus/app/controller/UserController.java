package com.valueplus.app.controller;

import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.model.ProfilePictureDto;
import com.valueplus.domain.model.UserUpdate;
import com.valueplus.domain.service.concretes.ProfilePictureService;
import com.valueplus.domain.service.concretes.UserService;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;
    private final ProfilePictureService profilePictureService;

    public UserController(UserService userService,
                          ProfilePictureService profilePictureService) {
        this.userService = userService;
        this.profilePictureService = profilePictureService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public Page<AgentDto> findAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return userService.findUsers(pageable)
                .map(AgentDto::valueOf);
    }

    @GetMapping("/{userId}")
    public AgentDto getUser(@PathVariable("userId") long userId) {
        log.debug("getUser() id = {}", userId);
        return userService.find(userId)
                .map(AgentDto::valueOf)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(method = RequestMethod.GET, path = "/current")
    public AgentDto getCurrentUser() throws IOException {
        User user = UserUtils.getLoggedInUser();
        String photo = profilePictureService.get(user)
                .map(ProfilePictureDto::valueOf)
                .map(ProfilePictureDto::getPhoto)
                .orElse(null);

        return AgentDto.valueOf(user, photo);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{userId}")
    public AgentDto update(@RequestParam("userId") long userId, @Valid @RequestBody UserUpdate userUpdate) {
        return AgentDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/current")
    public AgentDto update(@Valid @RequestBody UserUpdate userUpdate) {
        long userId = UserUtils.getLoggedInUser().getId();
        return AgentDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PostMapping("/update-pin")
    public AgentDto pinUpdate(@Valid @RequestBody PinUpdate pinUpdate) throws ValuePlusException {
        long userId = UserUtils.getLoggedInUser().getId();
        return AgentDto.valueOf(userService.pinUpdate(userId, pinUpdate));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<?> delete(@PathVariable("userId") Long userId) throws Exception {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
