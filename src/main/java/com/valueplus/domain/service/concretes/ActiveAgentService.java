package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.BadRequestException;
import com.valueplus.app.model.SuperAgentFilter;
import com.valueplus.domain.model.AgentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveAgentService {

    private final UserService userService;

    public Page<AgentDto> getAllActiveSuperAgents(SuperAgentFilter filter, Pageable pageable) {
        userService.findByReferralCode(filter.getSuperAgentCode().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid Super Agent Code"));

        return userService.findAllUserBySuperAgentCode(filter.getSuperAgentCode(), filter.getStartDate(), filter.getEndDate(), pageable)
                .map(u -> AgentDto.valueOf(u, userService.productUrlProvider()));
    }
}
