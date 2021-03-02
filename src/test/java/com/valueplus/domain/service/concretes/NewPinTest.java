package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.valueplus.fixtures.TestFixtures.mockUser;
import static com.valueplus.fixtures.TestFixtures.pinUpdate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class NewPinTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private NewPinService newPin;

    @Test
    void useStrategy() {
        var user = mockUser();
        user.setTransactionPin(null);

        assertThat(newPin.useStrategy(user)).isTrue();
    }

    @Test
    void dontUseStrategy() {
        var user = mockUser();
        user.setTransactionPin("jdhfhfhfhfhfhfhfhfh");

        assertThat(newPin.useStrategy(user)).isFalse();
    }

    @Test
    void validateFieldFails() {
        var user = mockUser();
        user.setTransactionPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword(null);

        assertThatThrownBy(() -> newPin.validateField(user, pinUpdateModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                .hasFieldOrPropertyWithValue("message", "Password is required when creating Pin");
    }

    @Test
    void validateField() {
        var user = mockUser();
        user.setTransactionPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("hdhgdhdhhd");

        assertDoesNotThrow(() -> newPin.validateField(user, pinUpdateModel));
    }

    @Test
    void updatePin_whenPasswordDoesNotMatch() {
        when(passwordEncoder.matches("plainPassword", "hashedPassword"))
                .thenReturn(false);

        var user = mockUser();
        user.setPassword("hashedPassword");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("plainPassword");

        assertThatThrownBy(() -> newPin.updatePin(user, pinUpdateModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("message", "Invalid password");

        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
    }

    @Test
    void updatePin_whenPasswordMatch() throws ValuePlusException {
        when(passwordEncoder.matches("plainPassword", "hashedPassword"))
                .thenReturn(true);
        when(passwordEncoder.encode("1234"))
                .thenReturn("HashedPin");

        var user = mockUser();
        user.setPassword("hashedPassword");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("plainPassword");
        pinUpdateModel.setNewPin("1234");

        var updatedUser = newPin.updatePin(user, pinUpdateModel);

        assertThat(updatedUser.getTransactionPin()).isEqualTo("HashedPin");
        assertThat(updatedUser.isTransactionTokenSet()).isTrue();
        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
        verify(passwordEncoder).encode("1234");
    }
}