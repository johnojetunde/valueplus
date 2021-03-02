package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.persistence.entity.User;
import org.springframework.http.HttpStatus;

import java.util.function.BiPredicate;

public interface PinUpdateService {
    boolean useStrategy(User user);

    default User updateOrCreatePin(User user, PinUpdate pinUpdate) throws ValuePlusException {
        validateField(user, pinUpdate);
        return updatePin(user, pinUpdate);
    }

    void validateField(User user, PinUpdate pinUpdate) throws ValuePlusException;

    User updatePin(User user, PinUpdate pinUpdate) throws ValuePlusException;

    default void ensureRequiredFieldIsSet(BiPredicate<User, PinUpdate> biPredicate,
                                          User user,
                                          PinUpdate pinUpdate,
                                          String errorMessage) throws ValuePlusException {
        if (biPredicate.test(user, pinUpdate))
            throw new ValuePlusException(errorMessage, HttpStatus.BAD_REQUEST);
    }
}
