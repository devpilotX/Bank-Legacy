package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.VerificationCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCaseRepository extends JpaRepository<VerificationCase, Long> {

    List<VerificationCase> findByWorkUnitIdOrderByCreatedAtAsc(Long workUnitId);
}
