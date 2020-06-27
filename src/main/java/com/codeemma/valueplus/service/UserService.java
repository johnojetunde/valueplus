package com.codeemma.valueplus.service;

import com.codeemma.valueplus.dto.UserDto;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> find(long userId) {
        return userRepository.findById(userId);
    }
}
