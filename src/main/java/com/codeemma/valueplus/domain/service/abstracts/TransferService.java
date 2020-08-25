package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.model.TransactionModel;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TransferService {
    TransactionModel transfer(User user, PaymentRequestModel requestModel) throws ValuePlusException;

    TransactionModel verify(User user, String referenceNumber) throws ValuePlusException;

    CompletableFuture<Void> verifyPendingTransactions();

    Page<TransactionModel> getAllUserTransactions(User user, Pageable pageable) throws ValuePlusException;

    Page<TransactionModel> getAllTransactions(Pageable pageable) throws ValuePlusException;

    Optional<TransactionModel> getTransactionByReference(User user, String reference) throws ValuePlusException;

    Page<TransactionModel> getTransactionBetween(User user,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable) throws ValuePlusException;
}
