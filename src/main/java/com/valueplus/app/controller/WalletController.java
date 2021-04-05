package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.WalletHistoryModel;
import com.valueplus.domain.model.WalletModel;
import com.valueplus.domain.service.abstracts.WalletHistoryService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "v1/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController {

    private final WalletService walletService;
    private final WalletHistoryService walletHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public WalletModel create() {
        User loggedInUser = UserUtils.getLoggedInUser();
        return walletService.createWallet(loggedInUser);
    }

    @PreAuthorize("hasAuthority('CREATE_WALLET_FOR_ALL_USERS')")
    @PostMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<WalletModel> createAllUserWallets() {
        return walletService.createWalletForAllUsers();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public WalletModel getWallet() throws Exception {
        User user = UserUtils.getLoggedInUser();
        return (UserUtils.isAgent(user))
                ? walletService.getWallet(user)
                : walletService.getWallet();
    }

    @PreAuthorize("hasAuthority('VIEW_ADMIN_WALLET_HISTORY')")
    @GetMapping("/admin/history")
    @ResponseStatus(HttpStatus.OK)
    public Page<WalletHistoryModel> getAdminWalletHistory(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return walletHistoryService.getHistory(pageable);
    }

    @PreAuthorize("hasAuthority('SEARCH_ADMIN_WALLET_HISTORY')")
    @PostMapping("/admin/history/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<WalletHistoryModel> getAdminWalletHistory(@RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                          @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                          @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return walletHistoryService.search(startDate, endDate, pageable);
    }

    @PreAuthorize("hasAuthority('VIEW_ALL_WALLET')")
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public Page<WalletModel> getAllWallet(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws Exception {
        return walletService.getAllWallet(pageable);
    }

    @GetMapping("/{walletId}/history")
    @ResponseStatus(HttpStatus.OK)
    public Page<WalletHistoryModel> getWalletHistory(@PathVariable("walletId") Long walletId,
                                                     @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        User user = UserUtils.getLoggedInUser();
        return walletHistoryService.getHistory(user, walletId, pageable);
    }

    @PostMapping("/history/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<WalletHistoryModel> getWalletHistory(@RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                     @RequestParam(value = "userId", required = false, defaultValue = "0") Long userId,
                                                     @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        User user = UserUtils.getLoggedInUser();
        if (UserUtils.isAgent(user)) {
            userId = user.getId();
        }
        return walletHistoryService.search(userId, startDate, endDate, pageable);
    }
}
