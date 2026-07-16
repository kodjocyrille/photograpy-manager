package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByFullNameAsc(UserRole role);

    List<User> findByRoleInOrderByFullNameAsc(List<UserRole> roles);

    List<User> findByActiveOrderByFullNameAsc(boolean active);

    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.active = true ORDER BY u.fullName")
    List<User> findActiveEmployees(@Param("roles") List<UserRole> roles);

    long countByRole(UserRole role);

    long countByActive(boolean active);
}
