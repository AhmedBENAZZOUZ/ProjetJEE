package tn.pi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.pi.entity.Patient;
import tn.pi.entity.Appointment;
import tn.pi.repository.PatientRepository;
import tn.pi.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class PatientController {
    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepository;

    public PatientController(PatientRepository patientRepo, AppointmentRepository appointmentRepository) {
        this.patientRepo = patientRepo;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("patient", new Patient());
        return "Home";
    }

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        if (session.getAttribute("loggedInPatient") != null) {
            model.addAttribute("loggedInPatient", session.getAttribute("loggedInPatient"));
            return "redirect:/";
        }
        model.addAttribute("patient", new Patient());
        return "Register";
    }

    @PostMapping("/register")
    public String registerPatient(@Valid Patient patient, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "Register";
        }
        try {
            patientRepo.save(patient);
            return "redirect:/";
        } catch (Exception e) {
            return "Register";
        }
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if (session.getAttribute("loggedInPatient") != null) {
            model.addAttribute("loggedInPatient", session.getAttribute("loggedInPatient"));
            return "redirect:/";
        }
        model.addAttribute("patient", new Patient());
        return "Login";
    }

    @PostMapping("/login")
    public String loginPatient(@Valid Patient patient, BindingResult bindingResult, HttpSession session) {
        if (patient.getEmail() == null || patient.getEmail().isEmpty()) {
            bindingResult.rejectValue("email", "error.patient", "Email cannot be empty");
            return "Login";
        }
        Optional<Patient> existingPatient = patientRepo.findByEmail(patient.getEmail());
        if (existingPatient.isEmpty() || !existingPatient.get().getPassword().equals(patient.getPassword())) {
            bindingResult.rejectValue("email", "error.patient", "Invalid email or password");
            return "Login";
        }
        Patient patientLogged = existingPatient.get();
        session.setAttribute("loggedInPatient", patientLogged);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        Patient loggedInPatient = (Patient) session.getAttribute("loggedInPatient");
        if (loggedInPatient == null) {
            return "redirect:/login";
        }

        // Fetch the latest data from the database
        Optional<Patient> freshPatient = patientRepo.findById(loggedInPatient.getId());
        if (freshPatient.isPresent()) {
            model.addAttribute("patient", freshPatient.get());
        }

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        
        // Get future appointments
        List<Appointment> futureAppointments = appointmentRepository.findByPatientAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(
            loggedInPatient, now);
        model.addAttribute("futureAppointments", futureAppointments);
        
        // Get past appointments
        List<Appointment> pastAppointments = appointmentRepository.findByPatientAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(
            loggedInPatient, now);
        model.addAttribute("pastAppointments", pastAppointments);

        // Debug log to check appointments
        System.out.println("Future appointments found: " + (futureAppointments != null ? futureAppointments.size() : 0));
        System.out.println("Past appointments found: " + (pastAppointments != null ? pastAppointments.size() : 0));
        
        return "Profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid Patient updatedPatient, BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "Profile";
        }

        Patient loggedInPatient = (Patient) session.getAttribute("loggedInPatient");
        if (loggedInPatient == null) {
            return "redirect:/login";
        }

        // Keep the ID, email, name, gender, and birthDate from the logged-in patient
        updatedPatient.setId(loggedInPatient.getId());
        updatedPatient.setEmail(loggedInPatient.getEmail());
        updatedPatient.setName(loggedInPatient.getName());
        updatedPatient.setGender(loggedInPatient.getGender());
        updatedPatient.setBirthDate(loggedInPatient.getBirthDate());

        // Only update password if a new one is provided
        if (updatedPatient.getPassword() == null || updatedPatient.getPassword().isEmpty()) {
            updatedPatient.setPassword(loggedInPatient.getPassword());
        }

        try {
            Patient savedPatient = patientRepo.save(updatedPatient);
            session.setAttribute("loggedInPatient", savedPatient);
            return "redirect:/profile?success";
        } catch (Exception e) {
            result.rejectValue("email", "error.patient", "Error updating profile: " + e.getMessage());
            return "Profile";
        }
    }

    @PostMapping("/appointment/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, HttpSession session, Model model) {
        Patient loggedInPatient = (Patient) session.getAttribute("loggedInPatient");
        if (loggedInPatient == null) {
            return "redirect:/login";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            // Verify the appointment belongs to the logged-in patient
            if (appointment.getPatient() != null && appointment.getPatient().getId().equals(loggedInPatient.getId())) {
                appointmentRepository.delete(appointment);
                model.addAttribute("success", "Appointment cancelled successfully");
            } else {
                model.addAttribute("error", "You can only cancel your own appointments");
            }
        } else {
            model.addAttribute("error", "Appointment not found");
        }

        return "redirect:/profile";
    }

}
