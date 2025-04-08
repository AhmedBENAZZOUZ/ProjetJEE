package tn.pi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tn.pi.entity.Doctor;
import tn.pi.entity.Patient;
import tn.pi.repository.DoctorRepository;

import java.util.Optional;

@Controller
public class DoctorController {

    private final DoctorRepository doctorRepository;
    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/doctor/register")
    public String register(HttpSession session, Model model) {
//        if (session.getAttribute("loggedInPatient") != null) {
//            model.addAttribute("loggedInPatient", session.getAttribute("loggedInPatient"));
//            return "redirect:/";
//        }
        model.addAttribute("doctor", new Doctor());
        return "RegisterDoctor";
    }

    @PostMapping("/doctor/register")
    public String registerDoctor(@Valid Doctor doctor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "RegisterDoctor";
        }
        try {
            doctorRepository.save(doctor);
            return "redirect:/";
        } catch (Exception e) {
            return "RegisterDoctor";
        }
    }

    @GetMapping("/doctor/login")
    public String login(HttpSession session, Model model) {
        if (session.getAttribute("loggedInDoctor") != null) {
            model.addAttribute("loggedInDoctor", session.getAttribute("loggedInDoctor"));
            return "redirect:/";
        }
        model.addAttribute("doctor", new Doctor());
        return "LoginDoctor";
    }

    @PostMapping("/doctor/login")
    public String loginDoctor(@Valid Doctor doctor, BindingResult bindingResult, HttpSession session) {
        if (doctor.getEmail() == null || doctor.getEmail().isEmpty()) {
            bindingResult.rejectValue("email", "error.doctor", "Email cannot be empty");
            return "LoginDoctor";
        }
        Optional<Doctor> existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
        if (existingDoctor.isEmpty() || !existingDoctor.get().getPassword().equals(doctor.getPassword())) {
            bindingResult.rejectValue("email", "error.doctor", "Invalid email or password");
            return "LoginDoctor";
        }
        Doctor doctorLogged = existingDoctor.get();
        session.setAttribute("loggedInDoctor", doctorLogged);
        return "redirect:/";
    }

    @GetMapping("/doctor")
    public String showDoctors() {
        return "Doctor";
    }
}
