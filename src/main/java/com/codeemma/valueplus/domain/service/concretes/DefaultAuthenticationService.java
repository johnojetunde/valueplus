package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.exception.ValuePlusRuntimeException;
import com.codeemma.valueplus.app.model.LoginResponseModel;
import com.codeemma.valueplus.domain.model.LoginForm;
import com.codeemma.valueplus.domain.service.abstracts.AuthenticationService;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static com.codeemma.valueplus.domain.util.UserUtils.isAdmin;
import static com.codeemma.valueplus.domain.util.UserUtils.isAgent;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final static String ERROR_MSG = "Invalid credentials";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenAuthenticationService tokenService;

    @Override
    public LoginResponseModel agentLogin(LoginForm loginForm) throws ValuePlusException {
        try {
            return loginUser(loginForm, this::ensureUserIsAgent);
        } catch (Exception e) {
            throw new ValuePlusException(ERROR_MSG, UNAUTHORIZED);
        }
    }

    @Override
    public LoginResponseModel adminLogin(LoginForm loginForm) throws ValuePlusException {
        try {
            return loginUser(loginForm, this::ensureUserIsAdmin);
        } catch (Exception e) {
            throw new ValuePlusException(ERROR_MSG, UNAUTHORIZED);
        }
    }

    private LoginResponseModel loginUser(LoginForm loginForm, Consumer<User> validateUserTypeFunction) {
        User user = getUser(loginForm.getEmail());

        validateUserTypeFunction.accept(user);

        matchPassword(loginForm.getPassword(), user.getPassword());
        return new LoginResponseModel(tokenService.generatorToken(user));
    }

    private User getUser(String email) throws ValuePlusRuntimeException {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(this::invalidCredentialException);
    }

    private ValuePlusRuntimeException invalidCredentialException() {
        return new ValuePlusRuntimeException(ERROR_MSG);
    }

    private void ensureUserIsAdmin(User user) throws ValuePlusRuntimeException {
        if (!isAdmin(user)) {
            throw invalidCredentialException();
        }
    }

    private void ensureUserIsAgent(User user) throws ValuePlusRuntimeException {
        if (!isAgent(user)) {
            throw invalidCredentialException();
        }
    }

    private void matchPassword(String plainPassword, String encryptedPassword) {
        if (!passwordEncoder.matches(plainPassword, encryptedPassword)) {
            throw invalidCredentialException();
        }
    }
}
