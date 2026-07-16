package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.billing.ServiceCatalog;
import com.giovanni.photograpy_manager.domain.client.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {
    List<ServiceCatalog> findByActiveTrueOrderByTypeAsc();
    List<ServiceCatalog> findByTypeAndActiveTrue(EventType type);
}
