package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.AccountModel;
import com.codeemma.valueplus.domain.dto.AccountRequest;
import com.codeemma.valueplus.persistence.entity.User;

public interface AccountService {
    AccountModel create(User user, AccountRequest request) throws ValuePlusException;

    AccountModel update(Long id,User user, AccountRequest request) throws ValuePlusException;

    AccountModel getAccount(User user) throws ValuePlusException;
}
