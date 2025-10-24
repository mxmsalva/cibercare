package com.cibercare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibercare.model.Notificacion;

public interface INotificacionRepository extends JpaRepository<Notificacion, Long>{
	 List<Notificacion> findByDoctorIdAndLeidaFalse(Long doctorId);
}
