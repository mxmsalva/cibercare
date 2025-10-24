package com.cibercare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibercare.model.EstadoCita;

public interface IEstadoCitaRepository extends JpaRepository<EstadoCita, Long>{
	Optional<EstadoCita> findByNombre(String nombre);
}
