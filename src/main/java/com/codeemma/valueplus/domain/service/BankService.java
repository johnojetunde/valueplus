package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.paystack.model.AccountNumberModel;
import com.codeemma.valueplus.paystack.model.BankModel;

import java.util.List;

public interface BankService {
    List<BankModel> getBanks() throws ValuePlusException;

    AccountNumberModel resolveAccountNumber(String accountNumber, String bankCode) throws ValuePlusException;
}
