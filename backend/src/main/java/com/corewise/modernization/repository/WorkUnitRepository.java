package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.WorkUnit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkUnitRepository extends JpaRepository<WorkUnit, Long> {

    List<WorkUnit> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
