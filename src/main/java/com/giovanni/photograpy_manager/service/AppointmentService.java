package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.appointment.Appointment;
import com.giovanni.photograpy_manager.domain.appointment.AppointmentStatus;
import com.giovanni.photograpy_manager.dto.AppointmentForm;
import com.giovanni.photograpy_manager.repository.AppointmentRepository;
import com.giovanni.photograpy_manager.repository.ClientRepository;
import com.giovanni.photograpy_manager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable : " + id));
    }

    public List<Appointment> findByDateRange(LocalDate start, LocalDate end) {
        return appointmentRepository.findByDateRange(
                start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    public List<Appointment> findByClient(Long clientId) {
        return appointmentRepository.findByClientIdOrderByStartTimeDesc(clientId);
    }

    public List<Appointment> findByPhotographer(Long photographerId) {
        return appointmentRepository.findByPhotographerIdOrderByStartTimeDesc(photographerId);
    }

    public boolean hasConflict(Long photographerId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (photographerId == null) return false;
        List<Appointment> conflicts = appointmentRepository.findConflicts(photographerId, start, end, excludeId);
        return !conflicts.isEmpty();
    }

    @Transactional
    public Appointment create(AppointmentForm form) {
        Appointment apt = Appointment.builder()
                .client(clientRepository.findById(form.getClientId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable")))
                .type(form.getType())
                .startTime(form.getStartTime())
                .endTime(form.getEndTime())
                .location(form.getLocation())
                .notes(form.getNotes())
                .status(AppointmentStatus.PENDING)
                .build();

        if (form.getPhotographerId() != null) {
            apt.setPhotographer(userRepository.findById(form.getPhotographerId())
                    .orElseThrow(() -> new EntityNotFoundException("Photographe introuvable")));
        }

        return appointmentRepository.save(apt);
    }

    @Transactional
    public Appointment update(Long id, AppointmentForm form) {
        Appointment apt = findById(id);
        apt.setClient(clientRepository.findById(form.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable")));
        apt.setType(form.getType());
        apt.setStartTime(form.getStartTime());
        apt.setEndTime(form.getEndTime());
        apt.setLocation(form.getLocation());
        apt.setNotes(form.getNotes());

        if (form.getPhotographerId() != null) {
            apt.setPhotographer(userRepository.findById(form.getPhotographerId())
                    .orElseThrow(() -> new EntityNotFoundException("Photographe introuvable")));
        } else {
            apt.setPhotographer(null);
        }

        return appointmentRepository.save(apt);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment apt = findById(id);
        apt.setStatus(newStatus);
        return appointmentRepository.save(apt);
    }

    @Transactional
    public Appointment reschedule(Long id, LocalDateTime newStart, LocalDateTime newEnd) {
        Appointment apt = findById(id);
        apt.setStartTime(newStart);
        apt.setEndTime(newEnd);
        apt.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(apt);
    }

    public long countThisMonth() {
        YearMonth ym = YearMonth.now();
        return appointmentRepository.countByMonth(ym.getMonthValue(), ym.getYear());
    }
}
