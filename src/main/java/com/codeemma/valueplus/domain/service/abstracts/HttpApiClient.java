package com.codeemma.valueplus.domain.service.abstracts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Collections.singletonList;

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
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(singletonList(MediaType.APPLICATION_JSON));
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
