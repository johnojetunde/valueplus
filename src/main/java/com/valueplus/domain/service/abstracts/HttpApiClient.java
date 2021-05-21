package com.valueplus.domain.service.abstracts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
public abstract class HttpApiClient {
    private final String consumer;
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public <T> T sendRequest(HttpMethod method,
                             String urlPath,
                             Object requestEntity,
                             Map<String, String> headers,
                             ParameterizedTypeReference<T> clazz) {
        String url = baseUrl.concat(urlPath);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.setAccept(singletonList(APPLICATION_JSON));
        httpHeaders.setAll(headers);

        HttpEntity<?> httpEntity = new HttpEntity<>(requestEntity, httpHeaders);

        ResponseEntity<T> responseEntity = this.restTemplate.exchange(
                url,
                method,
                httpEntity,
                clazz);

        log.info("{} API {} request to {} is successful", consumer, method.name(), urlPath);
        return responseEntity.getBody();
    }

    public <T> T sendRequest(HttpMethod method,
                             String urlPath,
                             Object requestEntity,
                             Map<String, String> headers) {
        ParameterizedTypeReference<T> typeReference = new ParameterizedTypeReference<>() {};
        return sendRequest(method, urlPath, requestEntity, headers, typeReference);
    }
}
