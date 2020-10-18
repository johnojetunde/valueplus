package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AccountSummary;
import com.codeemma.valueplus.persistence.entity.User;

public interface SummaryService {

    AccountSummary getSummary(User user) throws ValuePlusException;

    AccountSummary getSummaryAllUsers() throws ValuePlusException;
}
