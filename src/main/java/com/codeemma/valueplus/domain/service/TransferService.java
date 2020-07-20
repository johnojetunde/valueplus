package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.dto.TransactionModel;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {
    TransactionModel transfer(User user, PaymentRequestModel requestModel) throws ValuePlusException;

    Page<TransactionModel> getAllUserTransactions(User user, Pageable pageable) throws ValuePlusException;

    Page<TransactionModel> getAllTransactions(Pageable pageable) throws ValuePlusException;
}
