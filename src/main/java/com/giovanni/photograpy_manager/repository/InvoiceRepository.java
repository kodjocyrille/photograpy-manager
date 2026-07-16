package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.billing.Invoice;
import com.giovanni.photograpy_manager.domain.billing.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<Invoice> findAllByOrderByCreatedAtDesc();
    List<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE MONTH(p.paymentDate) = :month AND YEAR(p.paymentDate) = :year")
    BigDecimal revenueByMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT i FROM Invoice i WHERE i.status <> com.giovanni.photograpy_manager.domain.billing.InvoiceStatus.PAID " +
           "AND i.totalAmount > i.amountPaid ORDER BY i.createdAt DESC")
    List<Invoice> findUnpaidInvoices();

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.amountPaid), 0) FROM Invoice i " +
           "WHERE i.status <> com.giovanni.photograpy_manager.domain.billing.InvoiceStatus.PAID " +
           "AND i.totalAmount > i.amountPaid")
    BigDecimal totalUnpaidAmount();
}
