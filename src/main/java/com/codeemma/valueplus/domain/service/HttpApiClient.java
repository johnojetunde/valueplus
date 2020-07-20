package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.paystack.model.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public abstract class HttpApiClient {
    private final String consumer;
    private final RestTemplate restTemplate;
    private final String baseUrl;


    public <T> Optional<T> sendRequest(HttpMethod method,
                                       String urlPath,
                                       Object requestEntity,
                                       Map<String, String> headers,
                                       Class<T> clazz) {
        String url = baseUrl.concat(urlPath);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setAll(headers);

        HttpEntity<?> httpEntity = new HttpEntity<>(requestEntity, httpHeaders);

        ResponseEntity<T> responseEntity = this.restTemplate.exchange(
                url,
                method,
                httpEntity,
                clazz);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("{} API {} request to {} is successful",consumer, method.name(), urlPath);
            return ofNullable(responseEntity.getBody());
        }

        log.error("{} API request error {}",consumer, responseEntity);
        return empty();
    }

    public Optional<ResponseModel> sendRequest(HttpMethod method,
                                               String urlPath,
                                               Map<Object, Object> requestEntity,
                                               Map<String, String> headers) {
        return sendRequest(method, urlPath, requestEntity, headers, ResponseModel.class);
    }
}
