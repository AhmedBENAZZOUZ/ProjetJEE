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

import java.util.List;
import java.util.Optional;

@Controller
public class DoctorController {

    private final DoctorRepository doctorRepository;

    // Constructor injection for DoctorRepository
    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
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
    public String registerDoctor(@Valid Doctor doctor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "RegisterDoctor"; // Return registration page in case of validation errors
        }
        try {
            doctorRepository.save(doctor); // Save the doctor object to the repository
            return "redirect:/"; // Redirect to home page after successful registration
        } catch (Exception e) {
            return "RegisterDoctor"; // Return to registration page if an error occurs
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

    // Afficher le profil du docteur
    // Afficher le profil du docteur
    @GetMapping("/doctor/profile")
    public String showProfile(HttpSession session, Model model) {
        Doctor loggedInDoctor = (Doctor) session.getAttribute("loggedInDoctor");
        if (loggedInDoctor == null) {
            return "redirect:/doctor/login"; // Si le docteur n'est pas connecté, redirige vers la page de login
        }
        model.addAttribute("doctor", loggedInDoctor); // Ajouter l'objet doctor au modèle pour l'afficher
        return "ProfileDoctor"; // Retourner la vue du profil
    }

    // Mettre à jour les informations du docteur
    @PostMapping("/doctor/profile")
    public String updateProfile(@Valid Doctor updatedDoctor, BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "ProfileDoctor"; // Si des erreurs de validation existent, retourner la page de profil
        }

        Doctor loggedInDoctor = (Doctor) session.getAttribute("loggedInDoctor");
        if (loggedInDoctor == null) {
            return "redirect:/doctor/login"; // Si l'utilisateur n'est pas connecté, rediriger vers la page de login
        }

        // Sauvegarder les informations mises à jour dans la base de données
        updatedDoctor.setId(loggedInDoctor.getId()); // Assurez-vous que l'ID est bien défini
        System.out.println("Updated Doctor: " + updatedDoctor); // Ajouter un log pour vérifier les changements

        doctorRepository.save(updatedDoctor);

        // Vérifiez si l'objet a bien été sauvegardé dans la base de données
        Doctor savedDoctor = doctorRepository.findById(updatedDoctor.getId()).orElse(null);
        if (savedDoctor != null) {
            System.out.println("Saved Doctor: " + savedDoctor);
        } else {
            System.out.println("Failed to update the doctor.");
        }

        session.setAttribute("loggedInDoctor", updatedDoctor); // Mettre à jour la session avec les nouvelles données

        return "redirect:/doctor/profile?success"; // Rediriger avec un message de succès
    }



}
