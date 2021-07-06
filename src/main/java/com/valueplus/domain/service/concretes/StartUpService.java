package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.DeviceReport;
import com.valueplus.persistence.entity.ProductProviderUser;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.valueplus.domain.enums.ProductProvider.DATA4ME;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.util.concurrent.CompletableFuture.runAsync;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartUpService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final AuthorityRepository authorityRepository;
    private final ProductProviderUserRepository providerRepository;
    private final DeviceReportRepository deviceReportRepository;

    private static final String DEFAULT_ADMIN_EMAIL = "vpadmin@gmail.com";

    public CompletableFuture<Void> loadDefaultData() {
        return runAsync(() -> {
            Optional<Role> role = roleRepository.findByName("ADMIN");
            Role savedRole;
            if (role.isEmpty()) {
                savedRole = roleRepository.save(new Role("ADMIN"));
            } else {
                savedRole = role.get();
            }

            Optional<User> user = userRepository.findByEmailAndDeletedFalse(DEFAULT_ADMIN_EMAIL);
            if (user.isEmpty()) {
                log.info("creating default admin user");
                User userprofile = User.builder()
                        .firstname("ValuePlus")
                        .lastname("Admin")
                        .emailVerified(true)
                        .deleted(false)
                        .enabled(true)
                        .email(DEFAULT_ADMIN_EMAIL)
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
        })
                .thenCompose(__ -> setUpAllAuthoritiesForDefaultUser())
                .thenCompose(__ -> createProviderRecord())
                .thenCompose(__ -> updateDeviceReportRepository());
    }

    public CompletableFuture<Void> setUpAllAuthoritiesForDefaultUser() {
        return runAsync(() -> {
            Optional<User> user = userRepository.findByEmailAndDeletedFalse(DEFAULT_ADMIN_EMAIL);
            if (user.isPresent()) {
                var authorities = authorityRepository.findAll();
                var userEntity = user.get();
                userEntity.setAuthorities(authorities);

                userRepository.save(userEntity);
            }
        });
    }

    private CompletableFuture<Void> createProviderRecord() {
        return runAsync(() -> {
            List<User> users = userRepository.findUsersByDeletedFalse();

            for (User user : users) {
                if (user.getAgentCode() != null && user.getProductProviders().isEmpty()) {
                    var provider = new ProductProviderUser()
                            .setUser(user)
                            .setAgentCode(user.getAgentCode())
                            .setAgentUrl(user.getAgentCode())
                            .setProvider(DATA4ME);

                    providerRepository.save(provider);
                    userRepository.save(user);
                }
            }
        });
    }

    private CompletableFuture<Void> updateDeviceReportRepository() {
        return runAsync(() -> {
            List<DeviceReport> deviceReportsWithProvider = deviceReportRepository.findAllByProviderIsNotNull();
            if (deviceReportsWithProvider.isEmpty()) {
                List<DeviceReport> deviceReports = deviceReportRepository.findAll();

                emptyIfNullStream(deviceReports)
                        .forEach(deviceReport -> deviceReport.setProvider(DATA4ME));

                deviceReportRepository.saveAll(deviceReports);
            }
        });
    }
}
