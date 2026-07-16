package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.workspace.Dossier;
import com.giovanni.photograpy_manager.domain.workspace.DossierColumn;
import com.giovanni.photograpy_manager.domain.workspace.DossierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {

    @Query("SELECT d FROM Dossier d JOIN FETCH d.client LEFT JOIN FETCH d.photographer WHERE d.column = :column ORDER BY d.priority DESC, d.createdAt DESC")
    List<Dossier> findByColumnOrderByPriorityDescCreatedAtDesc(@Param("column") DossierColumn column);

    @Query("SELECT d FROM Dossier d JOIN FETCH d.client LEFT JOIN FETCH d.photographer LEFT JOIN FETCH d.comments WHERE d.id = :id")
    Optional<Dossier> findByIdWithComments(@Param("id") Long id);

    @Query("SELECT d FROM Dossier d LEFT JOIN FETCH d.statusHistory WHERE d.id = :id")
    Optional<Dossier> findByIdWithHistory(@Param("id") Long id);

    List<Dossier> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<Dossier> findAllByOrderByCreatedAtDesc();

    @Query("SELECT d FROM Dossier d JOIN FETCH d.client WHERE d.photographer.id = :photographerId ORDER BY d.createdAt DESC")
    List<Dossier> findByPhotographerIdWithClient(@Param("photographerId") Long photographerId);
    long countByStatus(DossierStatus status);
    long countByColumn(DossierColumn column);

    @Query("SELECT d FROM Dossier d WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:priority IS NULL OR d.priority = :priority) AND " +
           "(:photographerId IS NULL OR d.photographer.id = :photographerId) " +
           "ORDER BY d.priority DESC, d.createdAt DESC")
    List<Dossier> findFiltered(@Param("status") DossierStatus status,
                                @Param("priority") com.giovanni.photograpy_manager.domain.workspace.Priority priority,
                                @Param("photographerId") Long photographerId);
}
