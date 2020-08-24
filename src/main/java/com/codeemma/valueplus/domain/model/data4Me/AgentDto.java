package com.codeemma.valueplus.domain.model.data4Me;

import com.codeemma.valueplus.domain.model.UserCreate;
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

    public static AgentDto from(UserCreate userCreate) {
        return builder().email(userCreate.getEmail())
                .name(userCreate.getFirstname().concat(" "+userCreate.getLastname()))
                .cell(userCreate.getPhone())
                .address(userCreate.getAddress()).build();
    }
}
