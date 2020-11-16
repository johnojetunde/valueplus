package com.valueplus.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AgentCreate extends UserCreate {
    @NotEmpty
    @Size(min = 8, message = "minimum of 8 characters")
    private String password;
    @NotEmpty
    private String address;
}
