package com.codeemma.valueplus.domain.model.data4Me;

import com.codeemma.valueplus.domain.model.AgentCreate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AgentDto {
    private String name;
    private String email;
    private String cell;
    private String address;
    @Setter
    private String password;

    public static AgentDto from(AgentCreate agentCreate) {
        return builder().email(agentCreate.getEmail())
                .name(agentCreate.getFirstname().concat(" " + agentCreate.getLastname()))
                .cell(agentCreate.getPhone())
                .address(agentCreate.getAddress()).build();
    }
}
