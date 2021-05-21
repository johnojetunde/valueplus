package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.PaymentRequestModel;
import com.valueplus.domain.model.TransactionModel;
import com.valueplus.persistence.entity.User;
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

    Page<TransactionModel> filter(User user,
                                  String status,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  Pageable pageable) throws ValuePlusException;
}
