package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.billing.ClientPrestation;
import com.giovanni.photograpy_manager.domain.billing.PrestationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientPrestationRepository extends JpaRepository<ClientPrestation, Long> {
    List<ClientPrestation> findByClientIdOrderByAssignedAtDesc(Long clientId);
    List<ClientPrestation> findByServiceCatalogIdOrderByAssignedAtDesc(Long serviceCatalogId);
    long countByClientId(Long clientId);

    @Query("SELECT cp FROM ClientPrestation cp WHERE cp.scheduledDate BETWEEN :start AND :end AND cp.status <> com.giovanni.photograpy_manager.domain.billing.PrestationStatus.ANNULEE")
    List<ClientPrestation> findByScheduledDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT cp FROM ClientPrestation cp WHERE CAST(cp.scheduledDate AS LocalDate) = :date AND cp.status <> com.giovanni.photograpy_manager.domain.billing.PrestationStatus.ANNULEE")
    List<ClientPrestation> findByScheduledDate(@Param("date") LocalDate date);

    List<ClientPrestation> findByStatusOrderByScheduledDateAsc(PrestationStatus status);

    @Query("SELECT cp FROM ClientPrestation cp WHERE cp.status = com.giovanni.photograpy_manager.domain.billing.PrestationStatus.EN_COURS AND cp.invoice IS NULL")
    List<ClientPrestation> findEnCoursWithoutInvoice();
}
