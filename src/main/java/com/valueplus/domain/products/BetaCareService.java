package com.valueplus.domain.products;

import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.model.data4Me.ProductProviderAgentDto;
import com.valueplus.domain.service.abstracts.HttpApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class BetaCareService extends HttpApiClient {

    private final String username;
    private final String password;

    public BetaCareService(RestTemplate restTemplate,
                           @Value("${betacare.base-url}") String baseUrl,
                           @Value("${betacare.email}") String username,
                           @Value("${betacare.password}") String password) {
        super("betacare", restTemplate, baseUrl);
        this.username = username;
        this.password = password;
    }


    public String getToken() {
        Optional<String> token = authenticate();

        if (token.isEmpty()) {
            throw new ValuePlusRuntimeException("Error getting access token");
        }

        return token.get();
    }

    public Optional<AgentInfoModel> createAgent(String token, ProductProviderAgentDto agent) {
        try {
            ParameterizedTypeReference<AgentInfoModel> typeReference = new ParameterizedTypeReference<>() {
            };
            String requestPath = String.format("/register/%s/%s/%s/%s", token, agent.getName(), agent.getEmail(), agent.getPassword());
            var result = sendRequest(HttpMethod.GET, requestPath, agent, emptyMap(), typeReference);
            return ofNullable(result);
        } catch (Exception ex) {
            log.error("betacare create agent error - " + ex.getMessage());
            return empty();
        }
    }

    public Optional<AgentInfoModel> getAgentInfo(String token, final String email) {
        try {
            ParameterizedTypeReference<AgentInfoModel> typeReference = new ParameterizedTypeReference<>() {
            };

            String requestPath = String.format("/users/%s/%s", token, email);
            var result = sendRequest(HttpMethod.GET, requestPath, null, emptyMap(), typeReference);

            return ofNullable(result);
        } catch (Exception ex) {
            log.error("betacare retrieving agent info - " + ex.getMessage());
            return empty();
        }
    }

    public List<AgentReport> downloadAgentReport(final LocalDate reportDate) {
        Optional<String> token = authenticate();

        if (token.isEmpty()) {
            return emptyList();
        }

        LocalDate startDate = reportDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = reportDate.with(TemporalAdjusters.lastDayOfMonth());

        String requestUrl = String.format("/report/%s/%s/%s", token.get(), startDate, endDate);


        try {
            ParameterizedTypeReference<List<AgentReport>> typeReference = new ParameterizedTypeReference<>() {
            };

            return sendRequest(HttpMethod.GET, requestUrl, null, emptyMap(), typeReference);

        } catch (Exception ex) {
            log.error("betacae create agent error - " + ex.getMessage());
            return emptyList();
        }
    }

    private Optional<String> authenticate() {
        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("email", username);
        requestEntity.put("password", password);

        String requestPath = String.format("/auth/%s/%s", username, password);

        Optional<Map<String, String>> result = ofNullable(
                sendRequest(HttpMethod.GET, requestPath, requestEntity, emptyMap())
        );

        if (result.isPresent()) {
            log.info("login successful on betacare");
            return ofNullable(result.get().get("accessToken"));
        }

        return empty();
    }

    @lombok.Value
    public static class AgentInfoModel {
        String name;
        String email;
        String referralUrl;
        String agentCode;
    }

    @lombok.Value
    public static class AgentReport {
        String agentCode;
        Integer activeUser;
    }
}
