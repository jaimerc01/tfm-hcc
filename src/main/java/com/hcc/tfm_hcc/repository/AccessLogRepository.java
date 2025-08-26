package com.hcc.tfm_hcc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.AccessLog;

@Repository
public interface AccessLogRepository extends CrudRepository<AccessLog, UUID> {
	List<AccessLog> findByUsuarioIdOrderByTimestampDesc(String usuarioId);
	List<AccessLog> findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(String usuarioId, LocalDateTime desde, LocalDateTime hasta);
}
