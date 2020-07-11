package com.codeemma.valueplus.service;

import com.codeemma.valueplus.dto.data4Me.AgentCode;
import com.codeemma.valueplus.dto.data4Me.AgentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class Data4meService {
    @Value("${data4me.base-url}")
    private String baseUrl;
    @Value("${data4me.email}")
    private String username;
    @Value("${data4me.password}")
    private String password;
    @Autowired
    private RestTemplate restTemplate;

    private Optional<String> authenticate() {
        String url = baseUrl.concat("/login");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map requestEntity = new HashMap();
        requestEntity.put("email", username);
        requestEntity.put("password", password);

        HttpEntity<?> httpEntity = new HttpEntity<>(requestEntity, httpHeaders);

        ResponseEntity<Map> responseEntity = this.restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                Map.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("login successful on data4me");
            return Optional.ofNullable(responseEntity.getBody().get("api_token").toString());
        }
        log.error("data4me login error {}", responseEntity);

        return Optional.empty();
    }

    public Optional<AgentCode> createAgent(final AgentDto agentDto) {
        createPassword(agentDto);
        Optional<String> token = this.authenticate();

        if (!token.isPresent()) {
            return Optional.empty();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", "Bearer " + token.get());

        HttpEntity<AgentDto> request = new HttpEntity<>(agentDto, httpHeaders);

        try {
            ResponseEntity<AgentCode> response = restTemplate
                    .exchange(baseUrl.concat("/agent"), HttpMethod.POST, request, AgentCode.class);
            return Optional.ofNullable(response.getBody());

        } catch (Exception ex) {
            log.error("data4me create agent error - " + ex.getMessage());
            return Optional.empty();
        }
    }

    private void createPassword(AgentDto agentDto) {
        agentDto.setPassword(password + agentDto.getEmail().substring(0, 3));
    }
}
