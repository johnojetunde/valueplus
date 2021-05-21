package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.service.abstracts.PinUpdateService;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.BiPredicate;

import static java.lang.String.valueOf;

@Service
@RequiredArgsConstructor
public class UpdatePinService implements PinUpdateService {
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean useStrategy(User user) {
        return user.isTransactionTokenSet();
    }

    @Override
    public void validateField(User user, PinUpdate pinUpdate) throws ValuePlusException {
        BiPredicate<User, PinUpdate> predicate = (User u, PinUpdate p) ->
                u.isTransactionTokenSet() && p.getCurrentPin() == null;
        ensureRequiredFieldIsSet(predicate, user, pinUpdate, "Current pin is required");
    }

    @Override
    public User updatePin(User user, PinUpdate pinUpdate) throws ValuePlusException {
        if (!passwordEncoder.matches(valueOf(pinUpdate.getCurrentPin()), user.getTransactionPin())) {
            throw new ValuePlusException("Invalid current pin", HttpStatus.UNAUTHORIZED);
        }
        String hashPin = passwordEncoder.encode(pinUpdate.getNewPin());
        user.setTransactionPin(hashPin);

        return user;
    }
}
