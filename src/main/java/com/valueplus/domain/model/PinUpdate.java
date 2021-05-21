package com.valueplus.domain.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PinUpdate {
    private String password;
    private String currentPin;
    @NotBlank
    @Size(min = 4, max = 4)
    private String newPin;
}
