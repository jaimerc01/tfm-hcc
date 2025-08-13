package com.hcc.tfm_hcc.dto;

import lombok.Getter;
import lombok.Setter;

public class ChangePasswordRequest {
    @Getter @Setter
    private String currentPassword;
    @Getter @Setter
    private String newPassword;
}
