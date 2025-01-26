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

@Controller
public class DoctorController {

    private final DoctorRepository doctorRepository;
    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/registerDoctor")
    public String register(HttpSession session, Model model) {
//        if (session.getAttribute("loggedInPatient") != null) {
//            model.addAttribute("loggedInPatient", session.getAttribute("loggedInPatient"));
//            return "redirect:/";
//        }
        model.addAttribute("doctor", new Doctor());
        return "RegisterDoctor";
    }

    @PostMapping("/registerDoctor")
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
}
