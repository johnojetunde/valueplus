package com.valueplus.domain.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class UserAuthorityUpdate {
    @NotNull
    Set<Long> authorities;
}
