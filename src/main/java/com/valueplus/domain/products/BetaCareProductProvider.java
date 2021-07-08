package com.valueplus.domain.products;

import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentReport;
import com.valueplus.domain.model.data4Me.ProductProviderAgentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Service
public class BetaCareProductProvider implements ProductProviderService, ProductProviderUrlService {

    private final BetaCareService betaCareService;

    @Override
    public ProductProvider provider() {
        return ProductProvider.BETA_CARE;
    }

    @Override
    public String getReferralUrl(String agentCode, String agentUrl) {
        return agentUrl;
    }

    @Override
    public ProductProviderUserModel create(Object authDetails, ProductProviderUserModel user) {
        var request = ProductProviderAgentDto.from(user);

        return betaCareService.createAgent((String) authDetails, request)
                .map(s -> user.setAgentCode(s.getAgentCode())
                        .setReferralUrl(s.getReferralUrl())
                ).orElseThrow(() -> new ValuePlusRuntimeException(format("Error registering user for %s provider", provider())));
    }

    @Override
    public Optional<ProductProviderUserModel> get(Object authDetails, String email) {
        return betaCareService.getAgentInfo((String) authDetails, email)
                .map(this::toUserModel);
    }

    @Override
    public Object authenticate() {
        return betaCareService.getToken();
    }

    @Override
    public Set<AgentReport> downloadAgentReport(LocalDate reportDate) {
        return emptyIfNullStream(betaCareService.downloadAgentReport(reportDate))
                .map(s -> new AgentReport(s.getAgentCode(), generateDeviceId(s.getActiveUser())))
                .collect(Collectors.toSet());
    }

    private Set<String> generateDeviceId(Integer count) {
        if (count <= 0)
            return emptySet();

        return IntStream.range(1, count + 1)
                .boxed()
                .map(__ -> generateDeviceId())
                .collect(toSet());
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }

    private ProductProviderUserModel toUserModel(BetaCareService.AgentInfoModel infoModel) {
        return ProductProviderUserModel.builder()
                .email(infoModel.getEmail())
                .provider(provider())
                .agentCode(infoModel.getAgentCode())
                .referralUrl(infoModel.getReferralUrl())
                .build();
    }
}
