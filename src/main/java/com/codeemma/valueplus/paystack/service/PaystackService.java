package com.codeemma.valueplus.paystack.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.AccountModel;
import com.codeemma.valueplus.domain.service.abstracts.BankService;
import com.codeemma.valueplus.domain.service.abstracts.HttpApiClient;
import com.codeemma.valueplus.domain.service.abstracts.PaymentService;
import com.codeemma.valueplus.paystack.model.*;
import lombok.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.codeemma.valueplus.domain.util.FunctionUtil.convertToKobo;
import static java.lang.String.format;
import static java.lang.String.join;

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

        var type = new ParameterizedTypeReference<ResponseModel<List<BankModel>>>() {};
        ResponseModel<List<BankModel>> result = sendRequest(HttpMethod.GET, "/bank", null, header, type);

        if (!result.isStatus()) {
            throw new ValuePlusException(result.getMessage());
        }
        return result.getData();
    }

    @Override
    public AccountNumberModel resolveAccountNumber(String accountNumber, String bankCode) throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();
        String requestUrl = format("/bank/resolve?account_number=%s&bank_code=%s", accountNumber, bankCode);

        var type = new ParameterizedTypeReference<ResponseModel<AccountNumberModel>>() {};
        ResponseModel<AccountNumberModel> result = sendRequest(HttpMethod.GET, requestUrl, null, header, type);

        if (!result.isStatus()) {
            throw new ValuePlusException(result.getMessage());
        }

        return result.getData();
    }

    @Override
    public TransferResponse transfer(String accountNumber, String bankCode, BigDecimal amount) throws ValuePlusException {
        AccountNumberModel accountModel = resolveAccountNumber(accountNumber, bankCode);

        TransferRecipient recipient = createTransferRecipient(
                accountModel.getAccountNumber(),
                accountModel.getAccountName(),
                bankCode);

        //TODO: generate the reference code here
        String reference = UUID.randomUUID().toString();
        return initiateTransfer(recipient.getRecipientCode(), amount, config.getPaymentReason(), reference);
    }

    @Override
    public TransferResponse transfer(AccountModel accountModel, BigDecimal amount) throws ValuePlusException {
        TransferRecipient recipient = createTransferRecipient(
                accountModel.getAccountNumber(),
                accountModel.getAccountName(),
                accountModel.getBankCode());

        //TODO: generate the reference code here
        String reference = UUID.randomUUID().toString();
        return initiateTransfer(recipient.getRecipientCode(), amount, config.getPaymentReason(), reference);
    }

    public TransferRecipient createTransferRecipient(String accountNumber,
                                                     String accountName,
                                                     String bankCode) throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();

        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("account_number", accountNumber);
        requestEntity.put("name", accountName);
        requestEntity.put("bank_code", bankCode);
        requestEntity.put("currency", CURRENCY);
        requestEntity.put("type", TRANSFER_TYPE);

        var type = new ParameterizedTypeReference<ResponseModel<TransferRecipient>>() {};
        ResponseModel<TransferRecipient> result = sendRequest(HttpMethod.POST, "/transferrecipient", requestEntity, header, type);

        if (!result.isStatus()) {
            throw new ValuePlusException(result.getMessage());
        }
        return result.getData();

    }

    public TransferResponse initiateTransfer(String recipientCode,
                                             BigDecimal amount,
                                             String reason,
                                             String reference) throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();

        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("source", "balance");
        requestEntity.put("recipient", recipientCode);
        requestEntity.put("amount", convertToKobo(amount));
        requestEntity.put("reason", reason);
        requestEntity.put("reference", reference);

        var type = new ParameterizedTypeReference<ResponseModel<TransferResponse>>() {};
        ResponseModel<TransferResponse> result = sendRequest(HttpMethod.POST, "/transfer", requestEntity, header, type);

        if (!result.isStatus()) {
            throw new ValuePlusException(result.getMessage());
        }
        return result.getData();
    }

    @Override
    public TransferVerificationResponse verifyTransfer(@NonNull String reference) throws ValuePlusException {
        Map<String, String> header = prepareRequestHeader();
        String requestUrl = format("/transfer/verify/%s", reference);

        var type = new ParameterizedTypeReference<ResponseModel<TransferVerificationResponse>>() {};
        ResponseModel<TransferVerificationResponse> result = sendRequest(HttpMethod.GET, requestUrl, null, header, type);

        if (!result.isStatus()) {
            throw new ValuePlusException(result.getMessage());
        }
        return result.getData();
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
