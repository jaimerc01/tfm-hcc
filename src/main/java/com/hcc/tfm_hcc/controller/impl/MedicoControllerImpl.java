package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.MedicoController;

@RestController
@RequestMapping("/medico")
public class MedicoControllerImpl implements MedicoController {

    @Override
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK-MEDICO");
    }
}
