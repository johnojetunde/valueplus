package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.enums.TransactionStatus;
import com.codeemma.valueplus.domain.model.AccountModel;
import com.codeemma.valueplus.domain.model.TransactionModel;
import com.codeemma.valueplus.domain.service.abstracts.PaymentService;
import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.paystack.model.TransferVerificationResponse;
import com.codeemma.valueplus.persistence.entity.Account;
import com.codeemma.valueplus.persistence.entity.Transaction;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.AccountRepository;
import com.codeemma.valueplus.persistence.repository.TransactionRepository;
import com.codeemma.valueplus.persistence.specs.SearchCriteria;
import com.codeemma.valueplus.persistence.specs.SearchOperation;
import com.codeemma.valueplus.persistence.specs.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.codeemma.valueplus.domain.model.RoleType.AGENT;
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
        Optional<Transaction> transaction;
        if (isAgent(user)) {
            transaction = transactionRepository.findByUser_IdAndReference(user.getId(), referenceNumber);
        } else {
            transaction = transactionRepository.findByReference(referenceNumber);
        }

        var transactionEntity = transaction
                .orElseThrow(() -> new ValuePlusException("No transaction exists with this reference number", BAD_REQUEST));

        return verify(transactionEntity);
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
            return transactionRepository.findByUser_IdOrderByIdDesc(user.getId(), pageable)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Page<TransactionModel> getAllTransactions(Pageable pageable) throws ValuePlusException {
        try {
            return transactionRepository.findAllByOrderByIdDesc(pageable)
                    .map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Optional<TransactionModel> getTransactionByReference(User user, String reference) throws ValuePlusException {
        try {
            Optional<Transaction> transaction;
            if (isAgent(user)) {
                transaction = transactionRepository.findByUser_IdAndReference(user.getId(), reference);
            } else {
                transaction = transactionRepository.findByReference(reference);
            }
            return transaction.map(Transaction::toModel);
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
            Page<Transaction> transactions;
            if (isAgent(user)) {
                transactions = transactionRepository.findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(
                        user.getId(),
                        startDateTime,
                        endDateTime,
                        pageable);
            } else {
                transactions = transactionRepository.findByCreatedAtIsBetweenOrderByIdDesc(
                        startDateTime,
                        endDateTime,
                        pageable);
            }

            return transactions.map(Transaction::toModel);
        } catch (Exception e) {
            log.error(TRANSACTION_FETCH_ERROR_MSG, e);
            throw new ValuePlusException(TRANSACTION_FETCH_ERROR_MSG, e);
        }
    }

    @Override
    public Page<TransactionModel> filter(User user, TransactionStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) throws ValuePlusException {
        TransactionSpecification specification = buildSpecification(status, startDate, endDate, user);
        return transactionRepository.findAll(Specification.where(specification), pageable)
                .map(Transaction::toModel);
    }

    private TransactionSpecification buildSpecification(TransactionStatus status,
                                                        LocalDate startDate,
                                                        LocalDate endDate,
                                                        User user) {
        TransactionSpecification specification = new TransactionSpecification();
        if (status != null) {
            specification.add(new SearchCriteria<>("status", status.name(), SearchOperation.MATCH));
        }

        if (isAgent(user)) {
            specification.add(new SearchCriteria<>("user", user, SearchOperation.EQUAL));
        }

        if (startDate != null) {
            specification.add(new SearchCriteria<>(
                    "createdAt",
                    startDate.atTime(LocalTime.MIN),
                    SearchOperation.GREATER_THAN_EQUAL));
        }

        if (endDate != null) {
            specification.add(new SearchCriteria<>("createdAt",
                    endDate.atTime(LocalTime.MAX),
                    SearchOperation.LESS_THAN_EQUAL));
        }
        return specification;
    }

    private boolean isAgent(User user) {
        return AGENT.name().equals(user.getRole().getName());
    }

    private TransactionModel verify(Transaction transaction) throws ValuePlusException {
        TransferVerificationResponse response = paymentService.verifyTransfer(transaction.getReference());
        transaction.setStatus(response.getStatus());
        return transactionRepository.save(transaction).toModel();
    }
}
