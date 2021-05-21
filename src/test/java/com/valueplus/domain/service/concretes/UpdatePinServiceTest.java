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
class UpdatePinServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UpdatePinService pinService;

    @Test
    void dontUseStrategy() {
        var user = mockUser();
        user.setTransactionPin(null);

        assertThat(pinService.useStrategy(user)).isFalse();
    }

    @Test
    void useStrategy() {
        var user = mockUser();
        user.setTransactionPin("jdhfhfhfhfhfhfhfhfh");

        assertThat(pinService.useStrategy(user)).isTrue();
    }

    @Test
    void validateFieldFails() {
        var user = mockUser();
        user.setTransactionPin("hdhfhgfhfhfh");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin(null);
        pinUpdateModel.setPassword(null);

        assertThatThrownBy(() -> pinService.validateField(user, pinUpdateModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                .hasFieldOrPropertyWithValue("message", "Current pin is required");
    }

    @Test
    void validateField() {
        var user = mockUser();
        user.setTransactionPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin("1432");
        pinUpdateModel.setPassword(null);

        assertDoesNotThrow(() -> pinService.validateField(user, pinUpdateModel));
    }

    @Test
    void updatePin_whenPinDoesNotMatch() {
        when(passwordEncoder.matches("1432", "hashedCurrentPin"))
                .thenReturn(false);

        var user = mockUser();
        user.setPassword("hashedPassword");
        user.setTransactionPin("hashedCurrentPin");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin("1432");

        assertThatThrownBy(() -> pinService.updatePin(user, pinUpdateModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("message", "Invalid current pin");

        verify(passwordEncoder).matches("1432", "hashedCurrentPin");
    }

    @Test
    void updatePin_whenPinMatch() throws ValuePlusException {
        when(passwordEncoder.matches("1432", "hashedCurrentPin"))
                .thenReturn(true);
        when(passwordEncoder.encode("1244"))
                .thenReturn("HashedPin");

        var user = mockUser();
        user.setTransactionPin("hashedCurrentPin");

        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword(null);
        pinUpdateModel.setNewPin("1244");
        pinUpdateModel.setCurrentPin("1432");

        var updatedUser = pinService.updatePin(user, pinUpdateModel);

        assertThat(updatedUser.getTransactionPin()).isEqualTo("HashedPin");
        assertThat(updatedUser.isTransactionTokenSet()).isTrue();
        verify(passwordEncoder).matches("1432", "hashedCurrentPin");
        verify(passwordEncoder).encode("1244");
    }
}