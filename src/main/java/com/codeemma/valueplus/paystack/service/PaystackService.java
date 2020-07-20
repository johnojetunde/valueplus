package com.codeemma.valueplus.paystack.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.paystack.model.*;
import com.codeemma.valueplus.domain.service.BankService;
import com.codeemma.valueplus.domain.service.HttpApiClient;
import com.codeemma.valueplus.domain.service.PaymentService;
import com.codeemma.valueplus.paystack.model.*;
import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.ValidationException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Service
public class PaystackService extends HttpApiClient implements BankService, PaymentService {

    private static final String TRANSFER_TYPE = "nuban";
    private static final String CURRENCY = "NGN";
    private final PaystackConfig config;

    public PaystackService(RestTemplate restTemplate,
                           PaystackConfig config) {
        super("paystack", restTemplate, config.getBaseUrl());
        this.config = config;
    }

    @Override
    public List<BankModel> getBanks() throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();

        Optional<ResponseModel> result = sendRequest(HttpMethod.GET, "/bank", null, header);

        ResponseModel responseModel = result
                .orElseThrow(() ->
                        new ValuePlusException("Error fetching banks from paystack"));

        var model = ofNullable((List<BankModel>) responseModel.getData());

        return model.orElse(emptyList());
    }

    @Override
    public AccountNumberModel resolveAccountNumber(String accountNumber, String bankCode) throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();
        String requestUrl = format("/bank/resolve?account_number=%s&bank_code=%s", accountNumber, bankCode);

        Optional<ResponseModel> result = sendRequest(HttpMethod.GET, requestUrl, null, header);

        ResponseModel responseModel = result
                .orElseThrow(() ->
                        new ValuePlusException("Error verifying account number"));

        var model = ofNullable((AccountNumberModel) responseModel.getData());

        return model.orElseThrow(() -> new ValidationException("Error verifying account number"));
    }

    @Override
    public TransferResponse transfer(String accountNumber, String bankCode, BigDecimal amount) throws ValuePlusException {
        AccountNumberModel accountModel = resolveAccountNumber(accountNumber, bankCode);

        TransferRecipient recipient = createTransferRecipient(
                accountModel.getAccountNumber(),
                accountModel.getAccountName(),
                bankCode);

        //TODO: generate the reference code here
        return initiateTransfer(recipient.getRecipientCode(), amount, config.getPaymentReason(), "");
    }

    public TransferRecipient createTransferRecipient(String accountNumber,
                                                     String accountName,
                                                     String bankCode) throws ValuePlusException {
        final String msg = "Error creating transfer recipient";

        Map<String, String> header = prepareRequestHeader();

        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("account_number", accountNumber);
        requestEntity.put("name", accountName);
        requestEntity.put("bank_code", bankCode);
        requestEntity.put("currency", CURRENCY);
        requestEntity.put("type", TRANSFER_TYPE);

        Optional<ResponseModel> result = sendRequest(HttpMethod.POST, "/transferrecipient", requestEntity, header);

        ResponseModel responseModel = result
                .orElseThrow(() ->
                        new ValuePlusException(msg));

        var model = ofNullable((TransferRecipient) responseModel.getData());

        return model.orElseThrow(() -> new ValidationException(msg));
    }

    public TransferResponse initiateTransfer(String recipientCode,
                                             BigDecimal amount,
                                             String reason,
                                             String reference) throws ValuePlusException {
        final String msg = "Error initiating transfer to recipient";

        Map<String, String> header = prepareRequestHeader();

        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("source", "balance");
        requestEntity.put("recipient_code", recipientCode);
        requestEntity.put("amount", amount);
        requestEntity.put("reason", reason);
        requestEntity.put("reference", reference);

        Optional<ResponseModel> result = sendRequest(HttpMethod.POST, "/transfer", requestEntity, header);

        ResponseModel responseModel = result
                .orElseThrow(() ->
                        new ValuePlusException(msg));

        var model = ofNullable((TransferResponse) responseModel.getData());

        return model.orElseThrow(() -> new ValidationException(msg));
    }

    @Override
    public TransferVerificationResponse verifyTransfer(@NonNull String reference) throws ValuePlusException {
        final String msg = "Error verifying transfer";
        Map<String, String> header = prepareRequestHeader();
        String requestUrl = format("/transfer/verify/%s", reference);

        Optional<ResponseModel> result = sendRequest(HttpMethod.GET, requestUrl, null, header);

        ResponseModel responseModel = result
                .orElseThrow(() ->
                        new ValuePlusException(msg));

        var model = ofNullable((TransferVerificationResponse) responseModel.getData());

        return model.orElseThrow(() -> new ValidationException(msg));
    }

    private Map<String, String> prepareRequestHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", join(" ", "Bearer", getApiKey()));
        return header;
    }

    private String getApiKey() {
        return PaystackConfig.Domain.LIVE.equals(config.getDomain()) ? config.getLiveApiKey() : config.getTestApiKey();
    }
}
