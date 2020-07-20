package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.dto.TransactionModel;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.persistence.entity.Transaction;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
public class DefaultTransferService implements TransferService {
    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;
    private static final String TRANSACTION_FETCH_ERROR_MSG = "Unable to fetch transaction";

    @Override
    public TransactionModel transfer(User user, PaymentRequestModel requestModel) throws ValuePlusException {
        TransferResponse response = paymentService.transfer(requestModel.getAccountNumber(), requestModel.getBankCode(), requestModel.getAmount());

        Transaction transaction = Transaction.builder()
                .accountNumber(requestModel.getAccountNumber())
                .amount(response.getAmount())
                .bankCode(requestModel.getBankCode())
                .reference(response.getReference())
                .transferId(response.getId())
                .currency(response.getCurrency())
                .user(user)
                .status(response.getStatus())
                .build();

        return transactionRepository.save(transaction).toModel();
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
}
