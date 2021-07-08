package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.RoleType;
import com.valueplus.domain.products.ProductProviderService;
import com.valueplus.domain.products.ProductProviderUserModel;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.DeviceReport;
import com.valueplus.persistence.entity.ProductProviderUser;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.valueplus.domain.enums.ProductProvider.DATA4ME;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toSet;

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
    private final List<ProductProviderService> providerServices;

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
                .thenCompose(__ -> updateDeviceReportRepository())
                .thenCompose(__ -> registerAgentWithoutProviders());
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


    private CompletableFuture<Void> registerAgentWithoutProviders() {
        return runAsync(() -> {
            List<User> users = userRepository.findUserByRole_Name(RoleType.AGENT.name());
            Set<ProductProvider> providers = emptyIfNullStream(providerServices)
                    .map(ProductProviderService::provider)
                    .collect(toSet());

            var providerUsersToRegister = getProvidersToRegister(users, providers);

            if (providerUsersToRegister.isEmpty())
                return;


            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (ProductProviderService service : providerServices) {
                List<User> user = providerUsersToRegister.getOrDefault(service.provider(), new ArrayList<>());

                futures.add(registerAgent(service, user));
            }

            CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)).join();
        });
    }

    @SneakyThrows
    private CompletableFuture<Void> registerAgent(ProductProviderService providerService, List<User> users) {
        return runAsync(() -> {
            for (int i = 0; i < users.size(); i++) {
                registerAgent(providerService, users.get(i)).join();
                if (i % 5 == 0) {
                    sleep();
                }
            }
        });
    }

    @SneakyThrows
    private void sleep() {
        Thread.sleep(2000);
    }

    private CompletableFuture<Void> registerAgent(ProductProviderService providerService, User user) {
        return runAsync(() -> {
            var ppUM = ProductProviderUserModel.from(user)
                    .setPassword(UUID.randomUUID().toString());
            var agent = providerService.migrate(ppUM, log);
            var providerUser = ProductProviderUser.toNewEntity(agent.get())
                    .setUser(user);
            providerRepository.save(providerUser);
        });
    }

    private Map<ProductProvider, List<User>> getProvidersToRegister(List<User> users, Set<ProductProvider> providers) {
        Map<ProductProvider, List<User>> providerToRegister = new HashMap<>();

        for (User user : users) {
            var userProviders = emptyIfNullStream(user.getProductProviders())
                    .map(ProductProviderUser::getProvider)
                    .collect(Collectors.toSet());
            if (userProviders.size() == providers.size())
                continue;

            for (ProductProvider provider : providers) {
                if (!userProviders.contains(provider)) {
                    var providerUsers = providerToRegister.getOrDefault(provider, new ArrayList<>());
                    providerUsers.add(user);
                    providerToRegister.put(provider, providerUsers);
                }
            }
        }

        return providerToRegister;
    }
}
