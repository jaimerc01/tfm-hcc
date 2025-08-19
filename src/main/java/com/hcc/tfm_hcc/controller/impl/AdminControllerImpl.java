package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.AdminController;

@RestController
@RequestMapping("/admin")
public class AdminControllerImpl implements AdminController {

    @Override
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK-ADMIN");
    }
}
