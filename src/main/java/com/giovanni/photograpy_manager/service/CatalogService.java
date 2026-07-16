package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.billing.ServiceCatalog;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.repository.ServiceCatalogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceCatalogRepository catalogRepository;

    public List<ServiceCatalog> findAllActive() {
        return catalogRepository.findByActiveTrueOrderByTypeAsc();
    }

    public List<ServiceCatalog> findAll() {
        return catalogRepository.findAll();
    }

    public ServiceCatalog findById(Long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestation introuvable : " + id));
    }

    @Transactional
    public ServiceCatalog create(EventType type, String description, int minDuration, BigDecimal price) {
        ServiceCatalog item = ServiceCatalog.builder()
                .type(type)
                .description(description)
                .minDurationHours(minDuration)
                .basePrice(price)
                .build();
        return catalogRepository.save(item);
    }

    @Transactional
    public ServiceCatalog update(Long id, EventType type, String description, int minDuration, BigDecimal price) {
        ServiceCatalog item = findById(id);
        item.setType(type);
        item.setDescription(description);
        item.setMinDurationHours(minDuration);
        item.setBasePrice(price);
        return catalogRepository.save(item);
    }

    @Transactional
    public void toggleActive(Long id) {
        ServiceCatalog item = findById(id);
        item.setActive(!item.isActive());
        catalogRepository.save(item);
    }
}
