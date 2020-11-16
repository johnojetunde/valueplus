package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.AccountRequest;
import com.valueplus.persistence.entity.User;

public interface AccountService {
    AccountModel create(User user, AccountRequest request) throws ValuePlusException;

    AccountModel update(Long id,User user, AccountRequest request) throws ValuePlusException;

    AccountModel getAccount(User user) throws ValuePlusException;
}
