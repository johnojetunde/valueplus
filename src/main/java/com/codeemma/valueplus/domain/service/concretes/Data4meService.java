package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.domain.dto.data4Me.AgentCode;
import com.codeemma.valueplus.domain.dto.data4Me.AgentDto;
import com.codeemma.valueplus.domain.service.abstracts.HttpApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;

@Slf4j
@Service
public class Data4meService extends HttpApiClient {
    private final String username;
    private final String password;

    public Data4meService(RestTemplate restTemplate,
                          @Value("${data4me.base-url}") String baseUrl,
                          @Value("${data4me.email}") String username,
                          @Value("${data4me.password}") String password) {
        super("data4me", restTemplate, baseUrl);
        this.username = username;
        this.password = password;
    }

    private Optional<String> authenticate() {
        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("email", username);
        requestEntity.put("password", password);

        Optional<Map> result = Optional.ofNullable(sendRequest(HttpMethod.POST, "/login", requestEntity, emptyMap()));

        if (result.isPresent()) {
            log.info("login successful on data4me");
            return Optional.ofNullable(result.get().get("api_token").toString());
        }

        return empty();
    }

    public Optional<AgentCode> createAgent(final AgentDto agentDto) {
        createPassword(agentDto);
        Optional<String> token = authenticate();

        if (token.isEmpty()) {
            return empty();
        }

        var header = Map.of("Authorization", "Bearer " + token.get());

        try {
            AgentCode result = sendRequest(HttpMethod.POST, "/agent", agentDto, header);
            return Optional.of(result);
        } catch (Throwable ex) {
            log.error("data4me create agent error - " + ex.getMessage());
            return empty();
        }
    }

    private void createPassword(AgentDto agentDto) {
        agentDto.setPassword(password + agentDto.getEmail().substring(0, 3));
    }
}
