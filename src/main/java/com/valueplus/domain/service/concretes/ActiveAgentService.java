package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.BadRequestException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.SuperAgentFilter;
import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.service.abstracts.ProductOrderService;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNull;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveAgentService {

    private final UserService userService;
    private final ProductOrderService productOrderService;

    public List<AgentDto> getAllActiveSuperAgents(SuperAgentFilter filter) {
        var superAgent = userService.findByReferralCode(filter.getSuperAgentCode())
                .orElseThrow(() -> new BadRequestException("Invalid Super Agent Code"));

        List<User> allAgents = userService.findUserBySuperAgent(superAgent);

        return emptyIfNullStream(allAgents)
                .filter(ag -> isActiveAgent(ag, filter.getStartDate(), filter.getEndDate()))
                .map(AgentDto::valueOf)
                .collect(toList());
    }

    private boolean isActiveAgent(User agent, LocalDate startDate, LocalDate endDate) {
        try {
            var allProducts = productOrderService.getAllProducts(null, null, OrderStatus.COMPLETED, startDate, endDate, agent);
            return !emptyIfNull(allProducts).isEmpty();
        } catch (ValuePlusException e) {
            log.error("Error checking if Agent with id {} is active", agent.getId());
            return false;
        }
    }
}
