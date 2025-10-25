package com.cibercare.controller;

import com.cibercare.model.Doctor;
import com.cibercare.model.Especialidad;
import com.cibercare.model.Usuario;
import com.cibercare.repository.ICitaRepository;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IEspecialidadRepository;
import com.cibercare.repository.IUsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctores")
public class DoctorController {

	@Autowired
	private IDoctorRepository doctorRepository;

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IEspecialidadRepository especialidadRepository;

	@PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE', 'DOCTOR')")
	@GetMapping
	public List<Doctor> listarDoctores() {
	    return doctorRepository.findAll();
	}


	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<?> crearDoctor(@RequestBody Doctor doctor) {
		try {
			if (doctor.getUsuario() == null || doctor.getUsuario().getId() == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar el usuario (usuario.id)"));
			}
			if (doctor.getEspecialidad() == null || doctor.getEspecialidad().getId() == null) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "Debe especificar la especialidad (especialidad.id)"));
			}

			Usuario usuario = usuarioRepository.findById(doctor.getUsuario().getId())
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
			Especialidad especialidad = especialidadRepository.findById(doctor.getEspecialidad().getId())
					.orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

			if (doctorRepository.findAll().stream().anyMatch(d -> d.getUsuario().getId().equals(usuario.getId()))) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(Map.of("error", "El usuario ya tiene un registro de doctor"));
			}

			doctor.setUsuario(usuario);
			doctor.setEspecialidad(especialidad);

			Doctor nuevoDoctor = doctorRepository.save(doctor);
			return ResponseEntity.ok(Map.of("mensaje", "Doctor registrado correctamente", "doctor", nuevoDoctor));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Error al registrar el doctor: " + e.getMessage()));
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarDoctor(@PathVariable Long id, @RequestBody Doctor datos) {
		return doctorRepository.findById(id).map(doctor -> {
			if (datos.getDescripcion() != null) {
				doctor.setDescripcion(datos.getDescripcion());
			}
			if (datos.getEspecialidad() != null && datos.getEspecialidad().getId() != null) {
				Especialidad especialidad = especialidadRepository.findById(datos.getEspecialidad().getId())
						.orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
				doctor.setEspecialidad(especialidad);
			}
			doctorRepository.save(doctor);
			return ResponseEntity.ok(Map.of("mensaje", "Doctor actualizado correctamente", "doctor", doctor));
		}).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Doctor no encontrado")));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminarDoctor(@PathVariable Long id) {
		if (!doctorRepository.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Doctor no encontrado"));
		}
		doctorRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("mensaje", "Doctor eliminado correctamente"));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
	@GetMapping("/usuario/{username}")
	public ResponseEntity<?> obtenerDoctorPorUsuario(@PathVariable String username) {
		try {
			Usuario usuario = usuarioRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

			Doctor doctor = doctorRepository.findByUsuario(usuario).orElseThrow(
					() -> new RuntimeException("No se encontró un doctor asociado al usuario: " + username));

			return ResponseEntity.ok(doctor);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
	}
	
	

}
