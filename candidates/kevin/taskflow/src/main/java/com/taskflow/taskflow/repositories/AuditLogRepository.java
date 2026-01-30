package com.taskflow.taskflow.repositories;

import com.taskflow.taskflow.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{

}
