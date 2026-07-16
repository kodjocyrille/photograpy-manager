package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.accounting.AccountCategory;
import com.giovanni.photograpy_manager.domain.accounting.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountCategoryRepository extends JpaRepository<AccountCategory, Long> {
    List<AccountCategory> findByActiveTrueOrderByNameAsc();
    List<AccountCategory> findByTypeAndActiveTrueOrderByNameAsc(CategoryType type);
    List<AccountCategory> findAllByOrderByTypeAscNameAsc();
}
