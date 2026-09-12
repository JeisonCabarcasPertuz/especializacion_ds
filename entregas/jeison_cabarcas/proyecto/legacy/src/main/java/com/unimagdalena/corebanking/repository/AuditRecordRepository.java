package com.unimagdalena.corebanking.repository;

import com.unimagdalena.corebanking.entity.AuditRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> {
}
