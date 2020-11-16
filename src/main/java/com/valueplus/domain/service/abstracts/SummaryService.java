package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountSummary;
import com.valueplus.persistence.entity.User;

public interface SummaryService {

    AccountSummary getSummary(User user) throws ValuePlusException;

    AccountSummary getSummaryAllUsers() throws ValuePlusException;
}
