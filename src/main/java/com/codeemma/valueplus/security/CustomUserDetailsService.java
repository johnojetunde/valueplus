package com.codeemma.valueplus.security;

import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
@Transactional
public class CustomUserDetailsService implements CustomUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsernameAndDeletedFalse(username);
        if(isNull(user)){
            throw new UsernameNotFoundException("User '" + username + "' not found.");
        }
        return user;
    }
}
