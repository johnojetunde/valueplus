package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.LoginResponseModel;
import com.codeemma.valueplus.domain.model.LoginForm;

public interface AuthenticationService {

    LoginResponseModel agentLogin(LoginForm loginForm) throws ValuePlusException;

    LoginResponseModel adminLogin(LoginForm loginForm) throws ValuePlusException;
}
