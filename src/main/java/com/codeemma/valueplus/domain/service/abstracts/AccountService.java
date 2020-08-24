package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AccountModel;
import com.codeemma.valueplus.domain.model.AccountRequest;
import com.codeemma.valueplus.persistence.entity.User;

public interface AccountService {
    AccountModel create(User user, AccountRequest request) throws ValuePlusException;

    AccountModel update(Long id,User user, AccountRequest request) throws ValuePlusException;

    AccountModel getAccount(User user) throws ValuePlusException;
}
