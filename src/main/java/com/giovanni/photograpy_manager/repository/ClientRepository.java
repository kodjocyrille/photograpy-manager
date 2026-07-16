package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.client.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Client c WHERE c.archived = false AND " +
           "(LOWER(c.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "c.phone LIKE CONCAT('%',:q,'%')) " +
           "ORDER BY c.createdAt DESC")
    List<Client> search(@Param("q") String query);

    List<Client> findByArchivedFalseOrderByCreatedAtDesc();

    List<Client> findByServiceTypeAndArchivedFalse(EventType serviceType);

    long countByArchivedFalse();
}
