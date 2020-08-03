package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.dto.AccountModel;
import com.codeemma.valueplus.domain.dto.TransactionModel;
import com.codeemma.valueplus.domain.service.abstracts.PaymentService;
import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.paystack.model.TransferVerificationResponse;
import com.codeemma.valueplus.persistence.entity.Account;
import com.codeemma.valueplus.persistence.entity.Transaction;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.AccountRepository;
import com.codeemma.valueplus.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.codeemma.valueplus.domain.util.FunctionUtil.convertToNaira;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultTransferService implements TransferService {

    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private static final String TRANSACTION_FETCH_ERROR_MSG = "Unable to fetch transaction";

    @Override
    public TransactionModel transfer(User user, PaymentRequestModel requestModel) throws ValuePlusException {
        AccountModel accountModel = accountRepository.findByUser_Id(user.getId())
                .map(Account::toModel)
                .orElseThrow(() -> new ValuePlusException("User has no existing account", BAD_REQUEST));

        TransferResponse response = paymentService.transfer(accountModel, requestModel.getAmount());

        Transaction transaction = Transaction.builder()
                .accountNumber(accountModel.getAccountNumber())
                .amount(convertToNaira(response.getAmount()))
                .bankCode(accountModel.getBankCode())
                .reference(response.getReference())
                .transferId(response.getId())
                .currency(response.getCurrency())
                .user(user)
                .status(response.getStatus().toLowerCase())
                .build();

        return transactionRepository.save(transaction).toModel();
    }

    @Override
    public TransactionModel verify(User user, String referenceNumber) throws ValuePlusException {
        Transaction transaction = transactionRepository.findByUser_IdAndReference(user.getId(), referenceNumber)
                .orElseThrow(() -> new ValuePlusException("No transaction exists with this reference number", BAD_REQUEST));

        return verify(transaction);
    }

    @Override
    public CompletableFuture<Void> verifyPendingTransactions() {
        return runAsync(() -> {
            for (Transaction transaction : transactionRepository.findPendingTransactions()) {
                try {
                    verify(transaction);
                    Thread.sleep(10000);
                } catch (ValuePlusException | InterruptedException e) {
                    log.error("Error verifying transaction status", e);
                }
            }
        });
    }

    @Override
    public Page<TransactionModel> getAllUserTransactions(User user, Pageable pageable) throws ValuePlusException {
        try {
            return transactionRepository.findByUser_Id(user.getId(), pageable)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Page<TransactionModel> getAllTransactions(Pageable pageable) throws ValuePlusException {
        try {
            return transactionRepository.findAll(pageable)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Optional<TransactionModel> getTransactionByReference(User user, String reference) throws ValuePlusException {
        try {
            return transactionRepository.findByUser_IdAndReference(user.getId(), reference)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Page<TransactionModel> getTransactionBetween(User user,
                                                        LocalDate startDate,
                                                        LocalDate endDate,
                                                        Pageable pageable) throws ValuePlusException {
        try {
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
            return transactionRepository.findByUser_IdAndCreatedAtIsBetween(
                    user.getId(),
                    startDateTime,
                    endDateTime,
                    pageable)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    private TransactionModel verify(Transaction transaction) throws ValuePlusException {
        TransferVerificationResponse response = paymentService.verifyTransfer(transaction.getReference());
        transaction.setStatus(response.getStatus());
        return transactionRepository.save(transaction).toModel();
    }
}
