package tn.pi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tn.pi.entity.Patient;
import tn.pi.repository.PatientRepository;

import java.util.Optional;

@Controller
public class PatientController {

    private final PatientRepository patientRepo;
    public PatientController(PatientRepository patientRepository) {
        this.patientRepo = patientRepository;
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
        Patient patient = (Patient) session.getAttribute("loggedInPatient");
        if (patient == null) {
            return "redirect:/login";
        }
        model.addAttribute("patient", patient);
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

        // garder l'ancien ID et email
        updatedPatient.setId(loggedInPatient.getId());
        updatedPatient.setEmail(loggedInPatient.getEmail());

        patientRepo.save(updatedPatient);
        session.setAttribute("loggedInPatient", updatedPatient);

        return "redirect:/profile?success";
    }

}
