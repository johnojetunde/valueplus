package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.WalletModel;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletModel createWallet(User user);

    WalletModel getWallet(User user) throws ValuePlusException;

    Page<WalletModel> getAllWallet(Pageable pageable) throws ValuePlusException;

    List<WalletModel> createWalletForAllUsers();

    WalletModel creditWallet(User user, BigDecimal amount, String description);

    WalletModel debitWallet(User user, BigDecimal amount, String description) throws ValuePlusException;
}
