package com.cibercare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibercare.model.Doctor;
import com.cibercare.model.Usuario;

public interface IDoctorRepository extends JpaRepository<Doctor, Long>{
	Optional<Doctor> findByUsuario(Usuario usuario);
	Optional<Doctor> findByUsuarioUsername(String username);

}
