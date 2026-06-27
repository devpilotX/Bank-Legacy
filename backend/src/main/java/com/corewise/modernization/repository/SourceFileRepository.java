package com.corewise.modernization.repository;

import com.corewise.modernization.domain.model.SourceFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceFileRepository extends JpaRepository<SourceFile, Long> {

    List<SourceFile> findByProjectIdOrderByFilenameAsc(Long projectId);
}
