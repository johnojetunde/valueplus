package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.LoginResponseModel;
import com.valueplus.domain.model.LoginForm;

public interface AuthenticationService {

    LoginResponseModel agentLogin(LoginForm loginForm) throws ValuePlusException;

    LoginResponseModel adminLogin(LoginForm loginForm) throws ValuePlusException;
}
