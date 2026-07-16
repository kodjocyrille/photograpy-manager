package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.appointment.Appointment;
import com.giovanni.photograpy_manager.domain.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientIdOrderByStartTimeDesc(Long clientId);

    List<Appointment> findByPhotographerIdOrderByStartTimeDesc(Long photographerId);

    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :start AND a.startTime <= :end ORDER BY a.startTime")
    List<Appointment> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.photographer.id = :photoId " +
           "AND a.status <> com.giovanni.photograpy_manager.domain.appointment.AppointmentStatus.CANCELLED " +
           "AND ((a.startTime < :end AND a.endTime > :start)) " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    List<Appointment> findConflicts(@Param("photoId") Long photographerId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("excludeId") Long excludeId);

    long countByStatusAndStartTimeBetween(AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE MONTH(a.startTime) = :month AND YEAR(a.startTime) = :year")
    long countByMonth(@Param("month") int month, @Param("year") int year);
}
