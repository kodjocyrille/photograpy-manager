package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.billing.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);
}
