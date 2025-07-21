package com.hcc.tfm_hcc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private long expirationTime;
    
}
