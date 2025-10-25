package com.cibercare.controller;

import com.cibercare.dto.CitaRequest;
import com.cibercare.model.Cita;
import com.cibercare.model.EstadoCita;
import com.cibercare.model.Paciente;
import com.cibercare.model.Usuario;
import com.cibercare.repository.ICitaRepository;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IEstadoCitaRepository;
import com.cibercare.repository.IHorarioRepository;
import com.cibercare.repository.IPacienteRepository;
import com.cibercare.repository.IUsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

	@Autowired
	private IPacienteRepository pacienteRepository;

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IDoctorRepository doctorRepository;

	@Autowired
	private IHorarioRepository horarioRepository;

	@Autowired
	private IEstadoCitaRepository estadoCitaRepository;

	@Autowired
	private ICitaRepository citaRepository;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<Paciente> listarPacientes() {
		return pacienteRepository.findAll();
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<?> obtenerPaciente(@PathVariable Long id) {
		return pacienteRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<?> crearPaciente(@RequestBody Paciente paciente) {
		try {
			if (paciente.getUsuario() == null || paciente.getUsuario().getId() == null) {
				return ResponseEntity.badRequest().body("Debe especificar el usuario (usuario.id)");
			}

			Usuario usuario = usuarioRepository.findById(paciente.getUsuario().getId())
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			paciente.setUsuario(usuario);
			Paciente nuevo = pacienteRepository.save(paciente);
			return ResponseEntity.ok(nuevo);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al registrar paciente: " + e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarPaciente(@PathVariable Long id, @RequestBody Paciente pacienteActualizado) {
		return pacienteRepository.findById(id).map(pacienteExistente -> {
			pacienteExistente.setTelefono(pacienteActualizado.getTelefono());
			pacienteExistente.setDireccion(pacienteActualizado.getDireccion());
			pacienteExistente.setFechaNacimiento(pacienteActualizado.getFechaNacimiento());
			return ResponseEntity.ok(pacienteRepository.save(pacienteExistente));
		}).orElse(ResponseEntity.notFound().build());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminarPaciente(@PathVariable Long id) {
		if (!pacienteRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		pacienteRepository.deleteById(id);
		return ResponseEntity.ok("Paciente eliminado correctamente");
	}

	@PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
	@GetMapping("/username/{username}")
	public ResponseEntity<?> obtenerPorUsername(@PathVariable String username) {
		Paciente paciente = pacienteRepository.findByUsuario_Username(username)
				.orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
		return ResponseEntity.ok(paciente);
	}

	@PreAuthorize("hasRole('PACIENTE')")
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarCita(@RequestBody CitaRequest request) {
		Cita cita = new Cita();
		cita.setPaciente(pacienteRepository.findById(request.getPacienteId())
				.orElseThrow(() -> new RuntimeException("Paciente no encontrado")));
		cita.setDoctor(doctorRepository.findById(request.getDoctorId())
				.orElseThrow(() -> new RuntimeException("Doctor no encontrado")));
		cita.setHorario(horarioRepository.findById(request.getHorarioId())
				.orElseThrow(() -> new RuntimeException("Horario no encontrado")));

		EstadoCita estado = estadoCitaRepository.findByNombre("PENDIENTE")
				.orElseThrow(() -> new RuntimeException("Estado no encontrado"));
		cita.setEstado(estado);

		Cita nueva = citaRepository.save(cita);
		return ResponseEntity.ok(nueva);
	}

}
