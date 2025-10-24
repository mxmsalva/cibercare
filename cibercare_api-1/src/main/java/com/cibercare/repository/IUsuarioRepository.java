package com.cibercare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibercare.model.Usuario;

import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);

}
