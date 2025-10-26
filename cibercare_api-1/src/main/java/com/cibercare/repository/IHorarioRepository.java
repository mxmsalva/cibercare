package com.cibercare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibercare.model.Horario;

public interface IHorarioRepository extends JpaRepository<Horario, Long>{
	 List<Horario> findByDoctorId(Long doctorId);

}
