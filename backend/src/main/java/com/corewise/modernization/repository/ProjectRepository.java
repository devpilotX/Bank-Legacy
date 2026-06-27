package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByClientIdOrderByNameAsc(Long clientId);
}
