package tn.pi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tn.pi.entity.Doctor;
import tn.pi.repository.DoctorRepository;
import tn.pi.entity.Appointment;
import tn.pi.repository.AppointmentRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

@Controller
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    // Constructor injection for DoctorRepository and AppointmentRepository
    public DoctorController(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // Registration page for Doctor
    @GetMapping("/doctor/register")
    public String register(HttpSession session, Model model) {
        // Prevent patients from accessing the doctor registration page
        model.addAttribute("doctor", new Doctor());
        return "RegisterDoctor";
    }

    // Handle doctor registration form submission
    @PostMapping("/doctor/register")
    public String registerDoctor(@Valid Doctor doctor, BindingResult bindingResult, Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "RegisterDoctor";
        }

        // Check if email already exists
        Optional<Doctor> existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
        if (existingDoctor.isPresent()) {
            bindingResult.rejectValue("email", "error.doctor", "Email already exists");
            return "RegisterDoctor";
        }

        try {
            // Set default values for consultation times
            if (doctor.getStartWorkTime() == null) {
                doctor.setStartWorkTime(LocalTime.of(9, 0)); // 09:00
            }
            if (doctor.getEndConsultationTime() == null) {
                doctor.setEndConsultationTime(LocalTime.of(17, 0)); // 17:00
            }
            if (doctor.getConsultationDuration() == null) {
                doctor.setConsultationDuration(30);
            }

            doctorRepository.save(doctor);
            return "redirect:/doctor/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "RegisterDoctor";
        }
    }

    // Login page for Doctor
    @GetMapping("/doctor/login")
    public String login(HttpSession session, Model model) {
        // Redirect already logged-in doctor to the home page
        if (session.getAttribute("loggedInDoctor") != null) {
            return "redirect:/";
        }
        model.addAttribute("doctor", new Doctor());
        return "LoginDoctor"; // Return login page
    }

    // Handle doctor login form submission
    @PostMapping("/doctor/login")
    public String loginDoctor(@Valid Doctor doctor, BindingResult bindingResult, HttpSession session) {
        // Validate the email field
        if (doctor.getEmail() == null || doctor.getEmail().isEmpty()) {
            bindingResult.rejectValue("email", "error.doctor", "Email cannot be empty");
            return "LoginDoctor";
        }
        // Check if the doctor exists and password matches
        Optional<Doctor> existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
        if (existingDoctor.isEmpty() || !existingDoctor.get().getPassword().equals(doctor.getPassword())) {
            bindingResult.rejectValue("email", "error.doctor", "Invalid email or password");
            return "LoginDoctor";
        }
        Doctor doctorLogged = existingDoctor.get();
        session.setAttribute("loggedInDoctor", doctorLogged); // Store the logged-in doctor in the session
        return "redirect:/"; // Redirect to the home page
    }

    // Display list of doctors (not fully implemented in this controller, just a placeholder)
    @GetMapping("/doctor")
    public String showDoctors(
            @RequestParam(value = "doctorName", required = false, defaultValue = "") String doctorName,
            @RequestParam(value = "city", required = false, defaultValue = "") String city,
            @RequestParam(value = "specialty", required = false, defaultValue = "") String specialty,
            Model model) {
        // Fetch doctors from the database based on search criteria
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndCityContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(
                doctorName, city, specialty);

        // Add the doctors list to the model
        model.addAttribute("doctors", doctors);
        // Add search parameters back to the model to preserve form state
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("city", city);
        model.addAttribute("specialty", specialty);

        return "Doctor"; // Return the Doctor.html template
    }

    // Show the doctor's profile
    @GetMapping("/doctor/profile")
    public String showProfile(HttpSession session, Model model, @RequestParam(value = "setup", required = false) String setup) {
        Doctor loggedInDoctor = (Doctor) session.getAttribute("loggedInDoctor");
        if (loggedInDoctor == null) {
            return "redirect:/doctor/login"; // Redirect to login if the doctor is not logged in
        }
        
        // Fetch doctor's appointments
        List<Appointment> appointments = appointmentRepository.findByDoctorOrderByAppointmentDateTimeAsc(loggedInDoctor);
        
        model.addAttribute("doctor", loggedInDoctor);
        model.addAttribute("appointments", appointments);
        
        if ("true".equals(setup)) {
            model.addAttribute("setupMessage", "Please set up your working hours and consultation duration.");
        }
        return "ProfileDoctor";
    }

    // Update the doctor's profile
    @PostMapping("/doctor/profile")
    public String updateProfile(@Valid Doctor updatedDoctor, BindingResult bindingResult, HttpSession session, Model model) {
        Doctor loggedInDoctor = (Doctor) session.getAttribute("loggedInDoctor");
        if (loggedInDoctor == null) {
            return "redirect:/doctor/login"; // Redirect to login if the doctor is not logged in
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("doctor", updatedDoctor);
            return "ProfileDoctor"; // Return the profile page if there are validation errors
        }

        try {
            // Check for email uniqueness (excluding the current doctor)
            Optional<Doctor> existingDoctor = doctorRepository.findByEmail(updatedDoctor.getEmail());
            if (existingDoctor.isPresent() && !existingDoctor.get().getId().equals(loggedInDoctor.getId())) {
                bindingResult.rejectValue("email", "error.doctor", "Email already exists");
                model.addAttribute("doctor", updatedDoctor);
                return "ProfileDoctor";
            }

            // Update the doctor's details
            loggedInDoctor.setName(updatedDoctor.getName());
            loggedInDoctor.setEmail(updatedDoctor.getEmail());
            loggedInDoctor.setPhone(updatedDoctor.getPhone());
            loggedInDoctor.setAddress(updatedDoctor.getAddress());
            loggedInDoctor.setCity(updatedDoctor.getCity());
            loggedInDoctor.setSpecialty(updatedDoctor.getSpecialty());
            loggedInDoctor.setStartWorkTime(updatedDoctor.getStartWorkTime());
            loggedInDoctor.setEndConsultationTime(updatedDoctor.getEndConsultationTime());
            loggedInDoctor.setConsultationDuration(updatedDoctor.getConsultationDuration());

            // Only update the password if a new one is provided
            if (updatedDoctor.getPassword() != null && !updatedDoctor.getPassword().isEmpty()) {
                loggedInDoctor.setPassword(updatedDoctor.getPassword());
            }

            // Save the updated doctor to the database
            doctorRepository.save(loggedInDoctor);
            System.out.println("Updated Doctor: " + loggedInDoctor);

            // Verify the save operation
            Doctor savedDoctor = doctorRepository.findById(loggedInDoctor.getId()).orElse(null);
            if (savedDoctor != null) {
                System.out.println("Saved Doctor: " + savedDoctor);
            } else {
                System.out.println("Failed to update the doctor.");
            }

            session.setAttribute("loggedInDoctor", loggedInDoctor); // Update the session with the new data
            return "redirect:/doctor/profile?success"; // Redirect with a success message
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while updating the profile: " + e.getMessage());
            model.addAttribute("doctor", updatedDoctor);
            return "ProfileDoctor";
        }
    }
}
