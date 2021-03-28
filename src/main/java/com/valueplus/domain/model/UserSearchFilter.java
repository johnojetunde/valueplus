package com.valueplus.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserSearchFilter {
    private String firstname;
    private String lastname;
    private RoleType roleType;
    private String email;
    private String superAgentCode;
}
