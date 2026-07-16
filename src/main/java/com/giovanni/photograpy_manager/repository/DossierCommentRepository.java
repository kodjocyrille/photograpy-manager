package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.workspace.DossierComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierCommentRepository extends JpaRepository<DossierComment, Long> {
}
