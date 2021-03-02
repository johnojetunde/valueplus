package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.service.abstracts.PinUpdateService;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.BiPredicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class NewPinService implements PinUpdateService {
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean useStrategy(User user) {
        return !user.isTransactionTokenSet();
    }

    @Override
    public void validateField(User user, PinUpdate pinUpdate) throws ValuePlusException {
        BiPredicate<User, PinUpdate> predicate = (User u, PinUpdate p) ->
                !u.isTransactionTokenSet() && isNullOrEmpty(p.getPassword());
        ensureRequiredFieldIsSet(predicate, user, pinUpdate, "Password is required when creating Pin");
    }

    @Override
    public User updatePin(User user, PinUpdate pinUpdate) throws ValuePlusException {
        if (!passwordEncoder.matches(pinUpdate.getPassword(), user.getPassword())) {
            throw new ValuePlusException("Invalid password", UNAUTHORIZED);
        }
        String hashPin = passwordEncoder.encode(pinUpdate.getNewPin());
        user.setTransactionPin(hashPin);
        return user;
    }
}
