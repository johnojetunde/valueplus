package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.PaymentRequestModel;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.TransactionModel;
import com.valueplus.domain.service.abstracts.PaymentService;
import com.valueplus.domain.service.abstracts.SettingsService;
import com.valueplus.domain.service.abstracts.TransferService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.paystack.model.TransferResponse;
import com.valueplus.paystack.model.TransferVerificationResponse;
import com.valueplus.persistence.entity.Account;
import com.valueplus.persistence.entity.Transaction;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.AccountRepository;
import com.valueplus.persistence.repository.TransactionRepository;
import com.valueplus.persistence.specs.SearchCriteria;
import com.valueplus.persistence.specs.SearchOperation;
import com.valueplus.persistence.specs.TransactionSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.valueplus.domain.util.FunctionUtil.convertToNaira;
import static com.valueplus.domain.util.FunctionUtil.setScale;
import static com.valueplus.domain.util.UserUtils.isAgent;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Service
public class DefaultTransferService implements TransferService {

    private static final String TRANSACTION_FETCH_ERROR_MSG = "Unable to fetch transaction";
    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;
    private final SettingsService settingsService;

    public DefaultTransferService(PaymentService paymentService,
                                  TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  WalletService walletService,
                                  PasswordEncoder passwordEncoder,
                                  SettingsService settingsService) {
        this.paymentService = paymentService;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
        this.settingsService = settingsService;
    }

    @Override
    public TransactionModel transfer(User user, PaymentRequestModel requestModel) throws ValuePlusException {
        ensureUserHasPinSet(user);
        ensureUserPinIsMatching(user, requestModel);

        AccountModel accountModel = accountRepository.findByUser_Id(user.getId())
                .map(Account::toModel)
                .orElseThrow(() -> new ValuePlusException("User has no existing account", BAD_REQUEST));
        var settings = settingsService.getCurrentSetting()
                .orElseThrow(() -> new ValuePlusException("Unable to retrieve commission"));

        BigDecimal percentageCommission = settings.getCommissionPercentage().divide(BigDecimal.valueOf(100.00));
        BigDecimal transactionAmount = setScale(requestModel.getAmount());
        BigDecimal commission = setScale(transactionAmount.multiply(percentageCommission));
        BigDecimal actualTransferFee = setScale(transactionAmount.subtract(commission));

        TransferResponse response = paymentService.transfer(accountModel, actualTransferFee);
        walletService.debitWallet(user, transactionAmount, "Debit via Withdrawal from transfer module");
        walletService.creditAdminWallet(commission, "Credit via Withdrawal from transfer module");

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
    public Page<TransactionModel> filter(User user, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) throws ValuePlusException {
        TransactionSpecification specification = buildSpecification(status, startDate, endDate, user);
        return transactionRepository.findAll(Specification.where(specification), pageable)
                .map(Transaction::toModel);
    }

    private TransactionSpecification buildSpecification(String status,
                                                        LocalDate startDate,
                                                        LocalDate endDate,
                                                        User user) {
        TransactionSpecification specification = new TransactionSpecification();
        if (status != null) {
            specification.add(new SearchCriteria<>("status", status.toLowerCase(), SearchOperation.MATCH));
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

    private TransactionModel verify(Transaction transaction) throws ValuePlusException {
        TransferVerificationResponse response = paymentService.verifyTransfer(transaction.getReference());
        transaction.setStatus(response.getStatus());
        return transactionRepository.save(transaction).toModel();
    }

    private void ensureUserPinIsMatching(User user, PaymentRequestModel requestModel) throws ValuePlusException {
        if (!passwordEncoder.matches(requestModel.getPin(), user.getTransactionPin()))
            throw new ValuePlusException("Incorrect pin", UNAUTHORIZED);
    }

    private void ensureUserHasPinSet(User user) throws ValuePlusException {
        if (!user.isTransactionTokenSet())
            throw new ValuePlusException("You need to set your transaction pin", BAD_REQUEST);
    }
}
