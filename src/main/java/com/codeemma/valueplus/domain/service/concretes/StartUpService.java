package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.service.abstracts.WalletService;
import com.codeemma.valueplus.persistence.entity.Role;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.RoleRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartUpService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public CompletableFuture<Void> loadDefaultData() {
        return CompletableFuture.runAsync(() -> {
            Optional<Role> role = roleRepository.findByName("ADMIN");
            Role savedRole;
            if (role.isEmpty()) {
                savedRole = roleRepository.save(new Role("ADMIN"));
            } else {
                savedRole = role.get();
            }
            String email = "vpadmin@gmail.com";

            Optional<User> user = userRepository.findByEmailAndDeletedFalse(email);
            if (user.isEmpty()) {
                log.info("creating default admin user");
                User userprofile = User.builder()
                        .firstname("ValuePlus")
                        .lastname("Admin")
                        .emailVerified(true)
                        .deleted(false)
                        .enabled(true)
                        .email(email)
                        .password("$2a$10$cSJfJg1oMODysqTzFeuCKOaTDCqGAWNkuqlUaVH8deHi3sxY.cNZa")
                        .role(savedRole)
                        .build();

                userRepository.save(userprofile);

                try {
                    walletService.getWallet();
                } catch (ValuePlusException e) {
                    log.error("Error getting wallet", e);
                }
            }
        });
    }
}
