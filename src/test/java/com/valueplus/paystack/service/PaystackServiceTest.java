package com.valueplus.paystack.service;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.paystack.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySources({
        @TestPropertySource("classpath:application.properties"),
        @TestPropertySource("classpath:test.properties")
})
@SpringBootTest
class PaystackServiceTest {
    @Autowired
    private PaystackService paystackService;

    @Test
    void retrieveBank() throws ValuePlusException {
        List<BankModel> banks = paystackService.getBanks();
        Set<String> bankCodes = banks.stream()
                .map(BankModel::getCode)
                .collect(toSet());

        assertTrue(bankCodes.containsAll(asList("044", "057", "076", "030", "033", "011")));
    }

    @Test
    void validateAccountNumber() throws ValuePlusException {
        AccountNumberModel response = paystackService.resolveAccountNumber("0011841230", "044");

        assertEquals("0011841230", response.getAccountNumber());
        assertEquals("JOHN OLUWADAMILARE OJETUNDE", response.getAccountName());
    }

    @Test
    void validateAccountNumber_Fails() {
        assertThrows(HttpClientErrorException.class, () -> {
            paystackService.resolveAccountNumber("00118211230", "044");
        });
    }

    @Test
    void createRecipient() throws ValuePlusException {
        TransferRecipient response = paystackService.createTransferRecipient("0011841230", "JOHN OLUWADAMILARE OJETUNDE", "044");

        assertFalse(response.getRecipientCode().isEmpty());
        assertEquals("test", response.getDomain());
        assertEquals("0011841230", response.getDetails().getAccountNumber());
        assertEquals("044", response.getDetails().getBankCode());
    }

    @Test
    void createRecipient_Fails() {
        assertThrows(HttpClientErrorException.class, () -> {
            paystackService.createTransferRecipient("001841230", "JOHN OLUWADAMILARE OJETUNDE", "044");
        });
    }

    @Test
    void initiateTransfer() throws ValuePlusException {
        String reference = UUID.randomUUID().toString();
        TransferRecipient recipient = paystackService.createTransferRecipient("0011841230", "JOHN OLUWADAMILARE OJETUNDE", "044");
        TransferResponse response = paystackService.initiateTransfer(recipient.getRecipientCode(), BigDecimal.ONE, "ValuePlus", reference);

        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(reference, response.getReference());
        assertEquals("otp", response.getStatus());
    }

    @Test
    void initiateTransfer_Fails() {
        assertThrows(HttpClientErrorException.class, () -> {
            paystackService.initiateTransfer("invalidcode", BigDecimal.ONE, "ValuePlus", "reference");
        });
    }

    @Test
    void verifyTransfer() throws ValuePlusException {
        String reference = UUID.randomUUID().toString();
        TransferRecipient recipient = paystackService.createTransferRecipient("0011841230", "JOHN OLUWADAMILARE OJETUNDE", "044");
        paystackService.initiateTransfer(recipient.getRecipientCode(), BigDecimal.ONE, "ValuePlus", reference);
        TransferVerificationResponse verificationResponse = paystackService.verifyTransfer(reference);

        assertEquals(BigDecimal.valueOf(100), verificationResponse.getAmount());
        assertEquals(reference, verificationResponse.getReference());
        assertEquals("otp", verificationResponse.getStatus());
    }
}