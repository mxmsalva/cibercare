package com.cibercare.controller;

import com.cibercare.model.Cita;
import com.cibercare.model.Doctor;
import com.cibercare.model.EstadoCita;
import com.cibercare.model.Horario;
import com.cibercare.model.Paciente;
import com.cibercare.repository.ICitaRepository;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IEstadoCitaRepository;
import com.cibercare.repository.IHorarioRepository;
import com.cibercare.repository.IPacienteRepository;
import com.cibercare.service.NotificacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

	@Autowired
	private ICitaRepository citaRepository;

	@Autowired
	private IEstadoCitaRepository estadoCitaRepository;

	@Autowired
	private IPacienteRepository pacienteRepository;

	@Autowired
	private IHorarioRepository horarioRepository;

	@Autowired
	private IDoctorRepository doctorRepository;

	@Autowired
	private NotificacionService notificacionService;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
	@GetMapping
	public List<Cita> listarCitas() {
		return citaRepository.findAll();
	}

	@PreAuthorize("hasRole('PACIENTE')")
	@PutMapping("/{id}/cancelar")
	public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
		Cita cita = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

		EstadoCita cancelado = estadoCitaRepository.findByNombre("CANCELADO")
				.orElseThrow(() -> new RuntimeException("Estado 'CANCELADO' no encontrado"));

		cita.setEstado(cancelado);
		citaRepository.save(cita);
		notificacionService.enviarNotificacion(cita.getDoctor(),
				"El paciente " + cita.getPaciente().getUsuario() + " ha cancelado su cita del " + cita.getHorario());
		return ResponseEntity.ok("Cita cancelada y notificación enviada");
	}

	@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
	@PutMapping("/Doctor/{id}/revisar")
	public ResponseEntity<?> marcarCitaComoRevisada(@PathVariable Long id) {
		Cita cita = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
		EstadoCita revisado = estadoCitaRepository.findByNombre("ATENDIDO")
				.orElseThrow(() -> new RuntimeException("Estado 'ATENDIDO' no encontrado"));

		cita.setEstado(revisado);
		citaRepository.save(cita);
		messagingTemplate.convertAndSend("/topic/citas", cita);
		return ResponseEntity.ok(cita);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
	@GetMapping("/paciente/{id}")
	public ResponseEntity<?> listarPorPaciente(@PathVariable Long id) {
		List<Cita> citas = citaRepository.findByPacienteId(id);
		return ResponseEntity.ok(citas);
	}

	@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
	@GetMapping("/doctor/{doctorId}")
	public ResponseEntity<?> listarCitasPorDoctor(@PathVariable Long doctorId) {
		List<Cita> citas = citaRepository.findByDoctorId(doctorId);
		if (citas.isEmpty()) {
			return ResponseEntity.ok(Map.of("mensaje", "El doctor no tiene citas registradas."));
		}
		return ResponseEntity.ok(citas);
	}

	@GetMapping("/doctor/{doctorId}/fecha/{fecha}")
	public List<Cita> listarCitasPorDoctorYFecha(@PathVariable Long doctorId, @PathVariable String fecha) {
		LocalDate fechaConsulta = LocalDate.parse(fecha);
		return citaRepository.findByDoctorIdAndHorario_Fecha(doctorId, fechaConsulta);
	}

	@PostMapping
	public ResponseEntity<?> registrarCita(@RequestBody Map<String, Object> datos, Authentication auth) {
		try {
			System.out.println("🩺 Datos recibidos: " + datos);
			Object horarioObj = ((Map<?, ?>) datos.get("horario")).get("id");
			Object doctorObj = ((Map<?, ?>) datos.get("doctor")).get("id");

			Long horarioId = Long.parseLong(horarioObj.toString());
			Long doctorId = Long.parseLong(doctorObj.toString());
			String motivo = (String) datos.get("motivo");
			String username = auth.getName();
			Paciente paciente = pacienteRepository.findByUsuarioUsername(username)
					.orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
			Horario horario = horarioRepository.findById(horarioId)
					.orElseThrow(() -> new RuntimeException("Horario no encontrado"));
			Doctor doctor = doctorRepository.findById(doctorId)
					.orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
			EstadoCita estado = estadoCitaRepository.findById(1L)
					.orElseThrow(() -> new RuntimeException("Estado no encontrado"));
			Cita cita = new Cita();
			cita.setPaciente(paciente);
			cita.setHorario(horario);
			cita.setDoctor(doctor);
			cita.setEstado(estado);
			cita.setMotivo(motivo);
			Cita guardada = citaRepository.save(cita);
			return ResponseEntity.ok(guardada);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Error al registrar cita: " + e.getMessage());
		}
	}
	//socket
	@PutMapping("/{id}/estado")
	public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> datos) {
	    Cita cita = citaRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
	    String nuevoEstado = datos.get("estado");
	    EstadoCita estado = estadoCitaRepository.findByNombre(nuevoEstado)
	            .orElseThrow(() -> new RuntimeException("Estado no válido"));
	    cita.setEstado(estado);
	    Cita actualizada = citaRepository.save(cita);
	    messagingTemplate.convertAndSend("/topic/citas", actualizada);
	    return ResponseEntity.ok(actualizada);
	}
	
	@PreAuthorize("hasRole('DOCTOR')")
	@PostMapping("/crear")
	public ResponseEntity<?> crearHorarioComoDoctor(@RequestBody Horario horario, Authentication auth) {
	    String username = auth.getName();
	    Doctor doctor = doctorRepository.findByUsuarioUsername(username)
	            .orElseThrow(() -> new RuntimeException("Doctor no encontrado para el usuario autenticado"));
	    horario.setDoctor(doctor);
	    Horario nuevo = horarioRepository.save(horario);
	    return ResponseEntity.ok(Map.of("mensaje", "Horario creado correctamente", "horario", nuevo));
	}
	
	@PreAuthorize("hasRole('DOCTOR')")
	@GetMapping("/mis-horarios")
	public ResponseEntity<?> listarMisHorarios(Authentication auth) {
	    String username = auth.getName();
	    Doctor doctor = doctorRepository.findByUsuarioUsername(username)
	            .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
	    List<Horario> horarios = horarioRepository.findByDoctorId(doctor.getId());
	    return ResponseEntity.ok(horarios);
	}



}