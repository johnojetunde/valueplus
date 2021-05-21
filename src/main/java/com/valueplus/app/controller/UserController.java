package com.valueplus.app.controller;

import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.SuperAgentFilter;
import com.valueplus.domain.model.*;
import com.valueplus.domain.service.concretes.ActiveAgentService;
import com.valueplus.domain.service.concretes.ProfilePictureService;
import com.valueplus.domain.service.concretes.UserService;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

import static com.valueplus.domain.util.UserUtils.getLoggedInUser;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@Validated
@Slf4j
@RestController
@RequestMapping(path = "v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;
    private final ProfilePictureService profilePictureService;
    private final ActiveAgentService activeAgentService;

    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    @GetMapping
    public Page<AgentDto> findAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return userService.findUsers(pageable)
                .map(AgentDto::valueOf);
    }

    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    @PostMapping("/searches")
    public Page<AgentDto> searchUsers(@Valid @RequestBody UserSearchFilter searchFilter,
                                      @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return userService.searchUsers(searchFilter, pageable);
    }

    @PreAuthorize("hasAuthority('VIEW_SUPER_AGENTS')")
    @GetMapping("/super-agents")
    public Page<AgentDto> findAllSuperAgents(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return userService.findSuperAgentUsers(pageable)
                .map(AgentDto::valueOf);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SUPER_AGENTS','ROLE_SUPER_AGENT')")
    @GetMapping("/super-agents/{agentCode}/users")
    public Page<AgentDto> getUserBySuperAgentCode(@PathVariable("agentCode") String superAgentCode, @PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        log.debug("getUser() referralCode = {}", superAgentCode.toLowerCase());
        return userService.findAllUserBySuperAgentCode(superAgentCode.toLowerCase(), pageable)
                .map(AgentDto::valueOf);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SUPER_AGENTS','ROLE_SUPER_AGENT')")
    @PostMapping("/super-agents/filter-active-users")
    public Page<AgentDto> getUserBySuperAgentCode(@Valid @RequestBody SuperAgentFilter superAgentFilter, @PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return activeAgentService.getAllActiveSuperAgents(superAgentFilter, pageable);
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
    public AgentDto getCurrentUser() {
        User user = getLoggedInUser();
        String photo = profilePictureService.getImage(user)
                .orElse(null);

        return AgentDto.valueOf(user, photo);
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{userId}")
    public AgentDto update(@RequestParam("userId") long userId, @Valid @RequestBody UserUpdate userUpdate) {
        return AgentDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/current")
    public AgentDto update(@Valid @RequestBody UserUpdate userUpdate) {
        long userId = getLoggedInUser().getId();
        return AgentDto.valueOf(userService.update(userUpdate.toUser(userId)));
    }

    @PreAuthorize("hasAuthority('UPDATE_ADMIN_AUTHORITY')")
    @PostMapping("/{userId}/update-authority")
    public AgentDto updateUserAuthority(@PathVariable("userId") Long userId, @Valid @RequestBody UserAuthorityUpdate authorityUpdate) {
        return AgentDto.valueOf(userService.updateUserAuthority(userId, authorityUpdate.getAuthorities()));
    }

    @PreAuthorize("hasAuthority('DISABLE_USER')")
    @PostMapping("/{userId}/disable")
    public AgentDto disableUser(@PathVariable("userId") Long userId) throws ValuePlusException {
        return AgentDto.valueOf(userService.disableUser(userId));
    }

    @PreAuthorize("hasAuthority('ENABLE_USER')")
    @PostMapping("/{userId}/enable")
    public AgentDto enableUser(@PathVariable("userId") Long userId) throws ValuePlusException {
        return AgentDto.valueOf(userService.enableUser(userId));
    }

    @PreAuthorize("hasAuthority('UPDATE_ADMIN_AUTHORITY')")
    @GetMapping("/authorities")
    public List<AuthorityModel> getUserAuthorities() {
        return userService.getAllAuthorities();
    }

    @PreAuthorize("hasAuthority('UPDATE_ADMIN_AUTHORITY')")
    @GetMapping("/authorities/ui")
    public List<AuthorityModel> getUIUserAuthorities() {
        return userService.getAllUIAuthorities();
    }

    @PostMapping("/update-pin")
    public AgentDto pinUpdate(@Valid @RequestBody PinUpdate pinUpdate) throws Exception {
        long userId = getLoggedInUser().getId();
        return AgentDto.valueOf(userService.pinUpdate(userId, pinUpdate));
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<?> delete(@PathVariable("userId") Long userId) throws Exception {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
