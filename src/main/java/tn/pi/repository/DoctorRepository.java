package tn.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pi.entity.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>{
}
