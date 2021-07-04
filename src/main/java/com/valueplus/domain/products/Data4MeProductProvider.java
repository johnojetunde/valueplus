package com.valueplus.domain.products;

import com.fasterxml.jackson.databind.JavaType;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentReport;
import com.valueplus.domain.model.data4Me.AgentCode;
import com.valueplus.domain.model.data4Me.Data4meAgentDto;
import com.valueplus.domain.service.concretes.Data4meService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static com.valueplus.domain.enums.ProductProvider.DATA4ME;
import static com.valueplus.domain.util.MapperUtil.MAPPER;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class Data4MeProductProvider implements ProductProviderService, ProductProviderUrlService {

    private final Data4meService data4meService;
    private final String referralUrl;

    public Data4MeProductProvider(Data4meService data4meService,
                                  @Value("${provider.data4me.referralBaseUrl}") String referralUrl) {
        this.data4meService = data4meService;
        this.referralUrl = referralUrl;
    }

    @Override
    public ProductProvider provider() {
        return DATA4ME;
    }

    @Override
    public String getReferralUrl(String agentCode) {
        return Optional.ofNullable(agentCode)
                .map(s -> referralUrl.concat(agentCode))
                .orElse(agentCode);
    }

    @Override
    public ProductProviderUserModel create(Object authDetails, ProductProviderUserModel user) {
        var request = Data4meAgentDto.from(user);
        Map<String, String> authDetailHeader = toAuthHeader(authDetails);

        return data4meService.createAgent(authDetailHeader, request)
                .map(s -> user.setAgentCode(s.getCode()))
                .orElseThrow(() -> new ValuePlusRuntimeException(format("Error registering user for %s provider", provider())));
    }

    @Override
    public Optional<ProductProviderUserModel> get(Object authDetails, String email) {
        Map<String, String> authDetailHeader = toAuthHeader(authDetails);

        return data4meService.getAgentInfo(authDetailHeader, email)
                .map(this::toUser);
    }

    @Override
    public Map<String, String> authenticate() {
        return data4meService.buildHeader();
    }

    @Override
    public Set<AgentReport> downloadAgentReport(LocalDate reportDate) throws IOException {
        log.info("getting monthly agent report for {}", reportDate);
        var result = data4meService.downloadAgentReport(reportDate);

        if (result.isEmpty()) return Set.of();

        Set<AgentReport> reportContent = new HashSet<>();
        FileUtils.readLines(new File(result.get()))
                .forEach(line -> reportContent.add(toAgentReport(line.toString())));

        return reportContent;
    }

    private Map<String, String> toAuthHeader(Object authDetails) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Map.class, Object.class);

        return MAPPER.convertValue(authDetails, type);
    }

    private AgentReport toAgentReport(String file) {
        List<String> content = asList(file.split(","));
        String agentCode = content.get(0);
        Set<Integer> deviceIds = content.subList(1, content.size())
                .stream()
                .map(Integer::parseInt)
                .collect(toSet());

        return new AgentReport(agentCode, deviceIds);
    }

    private ProductProviderUserModel toUser(AgentCode a) {
        return ProductProviderUserModel.builder()
                .agentCode(a.getCode())
                .referralUrl(a.getCode())
                .provider(provider())
                .build();
    }
}
