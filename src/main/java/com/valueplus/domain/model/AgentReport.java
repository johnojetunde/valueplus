package com.valueplus.domain.model;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.util.Set;

@ToString
@Getter
@Value
public class AgentReport {
    String agentCode;
    Set<String> deviceIds;
}
