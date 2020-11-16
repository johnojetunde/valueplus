package com.valueplus.domain.model.data4Me;

import com.valueplus.domain.model.AgentCreate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Data4meAgentDto {
    private String name;
    private String email;
    private String cell;
    private String address;
    @Setter
    private String password;

    public static Data4meAgentDto from(AgentCreate agentCreate) {
        return builder().email(agentCreate.getEmail())
                .name(agentCreate.getFirstname().concat(" " + agentCreate.getLastname()))
                .cell(agentCreate.getPhone())
                .address(agentCreate.getAddress()).build();
    }
}
