package com.hcc.tfm_hcc.dto;

public class LoginUsuarioDTO {
    private String nif;
    private String password;

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
