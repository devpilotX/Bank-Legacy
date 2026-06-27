package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.DependencyNode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyNodeRepository extends JpaRepository<DependencyNode, Long> {

    List<DependencyNode> findByProjectIdOrderByNameAsc(Long projectId);

    Optional<DependencyNode> findByProjectIdAndNameAndKind(Long projectId, String name, String kind);
}
