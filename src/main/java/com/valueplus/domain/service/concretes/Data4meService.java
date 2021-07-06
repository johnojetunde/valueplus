package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.model.data4Me.AgentCode;
import com.valueplus.domain.model.data4Me.ProductProviderAgentDto;
import com.valueplus.domain.service.abstracts.HttpApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.*;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

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

    public Map<String, String> buildHeader() {
        Optional<String> token = authenticate();

        if (token.isEmpty()) {
            throw new ValuePlusRuntimeException("Error getting authentication token");
        }

        return Map.of("Authorization", "Bearer " + token.get());
    }

    public Optional<AgentCode> createAgent(Map<String, String> header, ProductProviderAgentDto agentDto) {
        try {
            ParameterizedTypeReference<AgentCode> typeReference = new ParameterizedTypeReference<>() {
            };
            var result = sendRequest(HttpMethod.POST, "/agent", agentDto, header, typeReference);
            return ofNullable(result);
        } catch (Exception ex) {
            log.error("data4me create agent error - " + ex.getMessage());
            return empty();
        }
    }

    public Optional<AgentCode> getAgentInfo(Map<String, String> header, final String email) {
        var agentInfoRequest = new AgentInfoModel(email);
        try {
            ParameterizedTypeReference<AgentCode> typeReference = new ParameterizedTypeReference<>() {
            };
            var result = sendRequest(HttpMethod.POST, "/agentinfo", agentInfoRequest, header, typeReference);
            return ofNullable(result);
        } catch (Exception ex) {
            log.error("data4me retrieving agent info - " + ex.getMessage());
            return empty();
        }
    }

    public Optional<String> downloadAgentReport(final LocalDate reportDate) {
        Optional<String> token = authenticate();

        if (token.isEmpty()) {
            return empty();
        }

        String formattedDate = reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        var header = Map.of("Authorization", "Bearer " + token.get());
        Map<String, String> requestBody = Map.of("date", formattedDate);

        try {
            ParameterizedTypeReference<byte[]> typeReference = new ParameterizedTypeReference<>() {
            };

            var result = sendRequest(HttpMethod.POST, "/agentreport", requestBody, header, typeReference);
            if (ofNullable(result).isPresent()) {
                String fileFullPath = "reports/Agent_report_" + formattedDate + ".csv";
                writeByteArrayToFile(new File(fileFullPath), result);
                return of(fileFullPath);
            }

            return empty();
        } catch (Exception ex) {
            log.error("data4me create agent error - " + ex.getMessage());
            return empty();
        }
    }

    private Optional<String> authenticate() {
        Map<Object, Object> requestEntity = new HashMap<>();
        requestEntity.put("email", username);
        requestEntity.put("password", password);

        Optional<Map> result = ofNullable(sendRequest(HttpMethod.POST, "/login", requestEntity, emptyMap()));

        if (result.isPresent()) {
            log.info("login successful on data4me");
            return ofNullable(result.get().get("api_token").toString());
        }

        return empty();
    }

    @lombok.Value
    private static class AgentInfoModel {
        String email;
    }
}
