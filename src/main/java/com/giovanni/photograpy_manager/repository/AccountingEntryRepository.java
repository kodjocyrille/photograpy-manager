package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.accounting.AccountingEntry;
import com.giovanni.photograpy_manager.domain.accounting.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    List<AccountingEntry> findAllByOrderByEntryDateDescCreatedAtDesc();

    List<AccountingEntry> findByTypeOrderByEntryDateDesc(EntryType type);

    @Query("SELECT e FROM AccountingEntry e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:from IS NULL OR e.entryDate >= :from) AND " +
           "(:to IS NULL OR e.entryDate <= :to) " +
           "ORDER BY e.entryDate DESC, e.createdAt DESC")
    List<AccountingEntry> findFiltered(@Param("type") EntryType type,
                                       @Param("categoryId") Long categoryId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountingEntry e WHERE e.type = :type")
    BigDecimal totalByType(@Param("type") EntryType type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountingEntry e WHERE e.type = :type " +
           "AND e.entryDate >= :from AND e.entryDate <= :to")
    BigDecimal totalByTypeAndPeriod(@Param("type") EntryType type,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount ELSE 0 END), 0) " +
           "FROM AccountingEntry e")
    BigDecimal balance();

    @Query("SELECT COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount ELSE 0 END), 0) " +
           "FROM AccountingEntry e WHERE e.entryDate >= :from AND e.entryDate <= :to")
    BigDecimal balanceByPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Monthly aggregation for Chart.js
    @Query("SELECT EXTRACT(MONTH FROM e.entryDate) as month, e.type, COALESCE(SUM(e.amount), 0) " +
           "FROM AccountingEntry e WHERE EXTRACT(YEAR FROM e.entryDate) = :year " +
           "GROUP BY EXTRACT(MONTH FROM e.entryDate), e.type ORDER BY month")
    List<Object[]> monthlyTotalsByYear(@Param("year") int year);
}
