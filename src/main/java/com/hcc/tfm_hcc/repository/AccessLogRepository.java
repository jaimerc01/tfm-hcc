package com.hcc.tfm_hcc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.AccessLog;

@Repository
public interface AccessLogRepository extends CrudRepository<AccessLog, Long> {
}
