package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.billing.Quote;
import com.giovanni.photograpy_manager.domain.billing.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<Quote> findAllByOrderByCreatedAtDesc();
    List<Quote> findByStatusOrderByCreatedAtDesc(QuoteStatus status);
    long countByStatus(QuoteStatus status);
}
