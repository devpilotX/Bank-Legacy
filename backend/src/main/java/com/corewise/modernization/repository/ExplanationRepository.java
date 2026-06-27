package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.Explanation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExplanationRepository extends JpaRepository<Explanation, Long> {

    List<Explanation> findBySourceFileIdOrderByCreatedAtDesc(Long sourceFileId);
}
