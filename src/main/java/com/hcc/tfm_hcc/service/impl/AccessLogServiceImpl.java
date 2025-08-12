package com.hcc.tfm_hcc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.service.AccessLogService;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Override
    public void log(AccessLog log) {
        accessLogRepository.save(log);
    }
}
