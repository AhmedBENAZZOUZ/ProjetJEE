package tn.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pi.entity.Appointment;
import tn.pi.entity.Doctor;
import tn.pi.entity.Patient;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientEmail(String patientEmail);
    List<Appointment> findByDoctorOrderByAppointmentDateTimeAsc(Doctor doctor);
    List<Appointment> findByPatientAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(Patient patient, LocalDateTime dateTime);
    List<Appointment> findByPatientAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(Patient patient, LocalDateTime dateTime);
} 