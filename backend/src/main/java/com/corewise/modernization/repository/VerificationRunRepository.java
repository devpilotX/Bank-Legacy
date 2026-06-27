package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.VerificationRun;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRunRepository extends JpaRepository<VerificationRun, Long> {

    Optional<VerificationRun> findFirstByCaseIdOrderByCreatedAtDesc(Long caseId);
}
