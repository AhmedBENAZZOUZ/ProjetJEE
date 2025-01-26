package tn.pi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
//    @NotBlank(message = "Governorate cannot be blank")
//    @Size(min = 2, max = 50, message = "Governorate must be between 2 and 50 characters")
//    private String Governorate;

    @NotBlank(message = "Speciality cannot be blank")
    @Size(min = 5, message = "Speciality must be minimum of 5 characters")
    private String speciality;

//    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "Phone number must be exactly 8 digits")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 100, message = "Address must be between 5 and 100 characters")
    private String address;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    @Column(nullable = false)
    private boolean active = false;
}
