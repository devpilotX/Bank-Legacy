package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.DependencyEdge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyEdgeRepository extends JpaRepository<DependencyEdge, Long> {

    List<DependencyEdge> findByProjectId(Long projectId);

    void deleteByProjectIdAndOrigin(Long projectId, String origin);

    Optional<DependencyEdge> findFirstByProjectIdAndFromNodeIdAndToNodeIdAndKind(
        Long projectId, Long fromNodeId, Long toNodeId, String kind);
}
