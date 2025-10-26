package com.cibercare.controller;

import com.cibercare.dto.CitaRequest;
import com.cibercare.dto.PacienteDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;


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

	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarPaciente(@PathVariable Long id, @RequestBody PacienteDTO dto) {
	    Optional<Paciente> opt = pacienteRepository.findById(id);
	    if (opt.isEmpty()) {
	        return ResponseEntity.notFound().build();
	    }

	    Paciente paciente = opt.get();
	    Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
	            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    paciente.setUsuario(usuario);
	    paciente.setTelefono(dto.getTelefono());
	    paciente.setDireccion(dto.getDireccion());
	    paciente.setFechaNacimiento(dto.getFechaNacimiento());

	    pacienteRepository.save(paciente);
	    return ResponseEntity.ok(Map.of("mensaje", "Paciente actualizado correctamente"));
	}


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarPaciente(@PathVariable Long id) {
        try {
            if (!pacienteRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Paciente no encontrado"));
            }

            pacienteRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Paciente eliminado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el paciente"));
        }
    }

	@PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
	@GetMapping("/username/{username}")
	public ResponseEntity<?> obtenerPorUsername(@PathVariable String username) {
		Paciente paciente = pacienteRepository.findByUsuarioUsername(username)
				.orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
		return ResponseEntity.ok(paciente);
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrarCita(@RequestBody CitaRequest request, Authentication authentication) {
	    String username = authentication.getName();
	    Paciente paciente = pacienteRepository.findByUsuarioUsername(username)
	        .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

	    Cita cita = new Cita();
	    cita.setPaciente(paciente);
	    cita.setDoctor(doctorRepository.findById(request.getDoctorId())
	        .orElseThrow(() -> new RuntimeException("Doctor no encontrado")));
	    cita.setHorario(horarioRepository.findById(request.getHorarioId())
	        .orElseThrow(() -> new RuntimeException("Horario no encontrado")));
	    cita.setEstado(estadoCitaRepository.findByNombre("PENDIENTE")
	        .orElseThrow(() -> new RuntimeException("Estado no encontrado")));

	    // 🟢 nuevo
	    cita.setMotivo(request.getMotivo() != null ? request.getMotivo() : "Consulta general");

	    return ResponseEntity.ok(citaRepository.save(cita));
	}



}
