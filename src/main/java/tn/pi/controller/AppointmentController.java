package tn.pi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.pi.entity.Appointment;
import tn.pi.entity.Doctor;
import tn.pi.entity.Patient;
import tn.pi.repository.AppointmentRepository;
import tn.pi.repository.DoctorRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentController(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/Confirmationn.html")
    public String showConfirmation(@RequestParam("doctorId") Long doctorId, Model model, HttpSession session) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isPresent()) {
            Doctor foundDoctor = doctor.get();
            model.addAttribute("doctor", foundDoctor);
            
            // Create new appointment
            Appointment appointment = new Appointment();
            
            // Check if patient is logged in
            Patient loggedInPatient = (Patient) session.getAttribute("loggedInPatient");
            if (loggedInPatient != null) {
                // Pre-fill appointment with patient's information
                appointment.setPatientName(loggedInPatient.getName());
                appointment.setPatientEmail(loggedInPatient.getEmail());
                appointment.setPatientPhone(loggedInPatient.getPhone());
                model.addAttribute("isPatientLoggedIn", true);
            } else {
                model.addAttribute("isPatientLoggedIn", false);
            }
            
            model.addAttribute("appointment", appointment);
            
            // Add doctor's schedule information
            model.addAttribute("startWorkTime", foundDoctor.getStartWorkTime());
            model.addAttribute("endConsultationTime", foundDoctor.getEndConsultationTime());
            model.addAttribute("consultationDuration", foundDoctor.getConsultationDuration());

            // Get today's appointments
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = todayStart.plusDays(7); // Get a week's worth of appointments
            List<Appointment> bookedAppointments = appointmentRepository.findByDoctorAndAppointmentDateTimeBetween(
                foundDoctor, todayStart, todayEnd);

            // Debug log
            System.out.println("Found " + bookedAppointments.size() + " booked appointments:");
            for (Appointment apt : bookedAppointments) {
                System.out.println("Booked: " + apt.getAppointmentDateTime());
            }

            // Convert booked appointments to time strings for easy comparison in JavaScript
            List<String> bookedTimes = bookedAppointments.stream()
                .map(apt -> apt.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .collect(Collectors.toList());

            System.out.println("Formatted booked times: " + bookedTimes);
            
            model.addAttribute("bookedAppointments", bookedTimes);
            return "Confirmationn";
        }
        return "redirect:/doctor";
    }

    @PostMapping("/appointment/book")
    public String bookAppointment(@Valid @ModelAttribute("appointment") Appointment appointment,
                                BindingResult bindingResult,
                                @RequestParam("doctorId") Long doctorId,
                                @RequestParam("dateTime") String dateTimeStr,
                                Model model,
                                HttpSession session) {
        
        if (bindingResult.hasErrors()) {
            Optional<Doctor> doctor = doctorRepository.findById(doctorId);
            if (doctor.isPresent()) {
                model.addAttribute("doctor", doctor.get());
            }
            return "Confirmationn";
        }

        try {
            // Parse the date and time with timezone consideration
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            
            // Convert to system's default timezone
            appointmentDateTime = appointmentDateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

            // Set the appointment details
            Optional<Doctor> doctor = doctorRepository.findById(doctorId);
            if (doctor.isPresent()) {
                Doctor foundDoctor = doctor.get();
                
                // Check if the time slot is already booked
                LocalDateTime slotStart = appointmentDateTime;
                LocalDateTime slotEnd = appointmentDateTime.plusMinutes(1);
                List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentDateTimeBetween(
                    foundDoctor, slotStart, slotEnd);
                
                if (!existingAppointments.isEmpty()) {
                    model.addAttribute("error", "Cette plage horaire est déjà réservée. Veuillez en choisir une autre.");
                    model.addAttribute("doctor", foundDoctor);
                    return "Confirmationn";
                }

                appointment.setDoctor(foundDoctor);
                appointment.setAppointmentDateTime(appointmentDateTime);

                // Set patient if logged in
                Patient loggedInPatient = (Patient) session.getAttribute("loggedInPatient");
                if (loggedInPatient != null) {
                    appointment.setPatient(loggedInPatient);
                }
                
                // Save the appointment
                Appointment savedAppointment = appointmentRepository.save(appointment);
                
                // Store the appointment in session for the success page
                session.setAttribute("lastAppointment", savedAppointment);
                
                return "redirect:/appointment/success";
            }
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while booking the appointment: " + e.getMessage());
        }

        return "Confirmationn";
    }

    @GetMapping("/appointment/success")
    public String showSuccessPage(Model model, HttpSession session) {
        // Retrieve the appointment from session
        Appointment appointment = (Appointment) session.getAttribute("lastAppointment");
        if (appointment == null) {
            return "redirect:/"; // Redirect to home if no appointment found
        }
        
        // Add the appointment to the model
        model.addAttribute("appointment", appointment);
        
        // Remove the appointment from session
        session.removeAttribute("lastAppointment");
        
        return "AppointmentSuccess";
    }
} 