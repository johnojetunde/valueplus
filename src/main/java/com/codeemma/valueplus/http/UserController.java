package com.codeemma.valueplus.http;


import com.codeemma.valueplus.dto.UserDto;
import com.codeemma.valueplus.exception.NotFoundException;
import com.codeemma.valueplus.service.UserService;
import com.codeemma.valueplus.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    @RequestMapping(method = RequestMethod.GET, path = "/current/details")
    public UserDto getUserByProfile() {
        return UserDto.valueOf(UserUtils.getLoggedInUser());
    }
}
