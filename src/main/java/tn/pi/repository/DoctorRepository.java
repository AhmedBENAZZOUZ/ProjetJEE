package tn.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pi.entity.Doctor;

import java.util.List;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>{
    Optional<Doctor> findByEmail(String email);

    List<Doctor> findByNameContainingIgnoreCaseAndCityContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(
            String name, String city, String specialty);
}
