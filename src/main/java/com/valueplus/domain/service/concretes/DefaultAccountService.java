package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.AccountRequest;
import com.valueplus.domain.service.abstracts.AccountService;
import com.valueplus.domain.service.abstracts.PaymentService;
import com.valueplus.paystack.model.AccountNumberModel;
import com.valueplus.persistence.entity.Account;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.valueplus.domain.enums.ActionType.ACCOUNT_CREATE;
import static com.valueplus.domain.enums.EntityType.ACCOUNT;
import static com.valueplus.domain.util.MapperUtil.copy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class DefaultAccountService implements AccountService {
    private final PaymentService paymentService;
    private final AccountRepository repository;
    private final AuditEventPublisher auditEvent;

    @Override
    public AccountModel create(User user, AccountRequest request) throws ValuePlusException {
        var existingAccount = repository.findByUser_Id(user.getId());
        if (existingAccount.isPresent()) {
            throw new ValuePlusException("There is an existing account for the user", BAD_REQUEST);
        }

        AccountNumberModel model = paymentService.resolveAccountNumber(request.getAccountNumber().trim(), request.getBankCode());

        try {
            Account account = Account.builder()
                    .accountName(model.getAccountName())
                    .accountNumber(model.getAccountNumber().trim())
                    .bankCode(request.getBankCode())
                    .user(user)
                    .build();

            var savedAccount = repository.save(account).toModel();

            auditEvent.publish(new Object(), savedAccount, ACCOUNT_CREATE, ACCOUNT);
            return savedAccount;
        } catch (Exception e) {
            throw new ValuePlusException("Error adding account to profile", e);
        }
    }

    @Override
    public AccountModel update(Long id, User user, AccountRequest request) throws ValuePlusException {
        Account account = repository.findById(id)
                .orElseThrow(() -> new ValuePlusException("Invalid account id", BAD_REQUEST));

        if (account.getAccountNumber().equals(request.getAccountNumber())) {
            return account.toModel();
        }

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ValuePlusException("Invalid account update request", UNAUTHORIZED);
        }

        var oldObject = copy(account, Account.class);
        AccountNumberModel model = paymentService.resolveAccountNumber(request.getAccountNumber(), request.getBankCode());

        try {
            account.setAccountName(model.getAccountName());
            account.setAccountNumber(model.getAccountNumber());
            account.setBankCode(request.getBankCode());

            var savedAccount = repository.save(account).toModel();

            auditEvent.publish(oldObject, savedAccount, ACCOUNT_CREATE, ACCOUNT);
            return savedAccount;
        } catch (Exception e) {
            throw new ValuePlusException("Error updating account details", e);
        }
    }

    @Override
    public AccountModel getAccount(User user) throws ValuePlusException {
        return repository.findByUser_Id(user.getId())
                .map(Account::toModel)
                .orElseThrow(() -> new ValuePlusException("No existing account details", BAD_REQUEST));
    }
}
