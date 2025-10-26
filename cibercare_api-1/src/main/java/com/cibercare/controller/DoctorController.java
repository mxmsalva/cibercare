package com.cibercare.controller;

import com.cibercare.dto.DoctorDTO;
import com.cibercare.model.Doctor;
import com.cibercare.model.Especialidad;
import com.cibercare.model.Horario;
import com.cibercare.model.Usuario;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IEspecialidadRepository;
import com.cibercare.repository.IHorarioRepository;
import com.cibercare.repository.IUsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private IHorarioRepository horarioRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE', 'DOCTOR')")
    @GetMapping
    public List<Doctor> listarDoctores() {
        return doctorRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> crearDoctor(@RequestBody DoctorDTO dto) {
        try {
            if (dto.getUsuarioId() == null || dto.getEspecialidadId() == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Faltan datos obligatorios (usuarioId o especialidadId)")
                );
            }
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                    .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            Doctor existente = doctorRepository.findAll().stream()
                    .filter(d -> d.getUsuario().getId().equals(usuario.getId()))
                    .findFirst()
                    .orElse(null);

            if (existente != null) {
                existente.setDescripcion(dto.getDescripcion());
                existente.setEspecialidad(especialidad);
                doctorRepository.save(existente);
                return ResponseEntity.ok(
                    Map.of("mensaje", "Doctor actualizado correctamente", "doctor", existente)
                );
            }
            Doctor nuevoDoctor = new Doctor();
            nuevoDoctor.setUsuario(usuario);
            nuevoDoctor.setEspecialidad(especialidad);
            nuevoDoctor.setDescripcion(dto.getDescripcion());
            doctorRepository.save(nuevoDoctor);
            return ResponseEntity.ok(
                Map.of("mensaje", "Doctor registrado correctamente", "doctor", nuevoDoctor)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error al registrar o actualizar el doctor: " + e.getMessage())
            );
        }
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearHorario(@RequestBody Horario horario, Authentication auth) {
        try {
            String username = auth.getName();

            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
                Doctor doctor = doctorRepository.findByUsuarioUsername(username)
                        .orElseThrow(() -> new RuntimeException("Doctor no encontrado para el usuario autenticado"));
                horario.setDoctor(doctor);
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (horario.getDoctor() == null || horario.getDoctor().getId() == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar el ID del doctor"));
                }
                Doctor doctor = doctorRepository.findById(horario.getDoctor().getId())
                        .orElseThrow(() -> new RuntimeException("Doctor no encontrado con ID: " + horario.getDoctor().getId()));
                horario.setDoctor(doctor);
            }
            horario.setDisponible(true);
            Horario nuevoHorario = horarioRepository.save(horario);
            return ResponseEntity.ok(Map.of("mensaje", "Horario creado correctamente", "horario", nuevoHorario));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear horario: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDoctor(@PathVariable Long id, @RequestBody DoctorDTO dto) {
        return doctorRepository.findById(id).map(doctor -> {
            if (dto.getDescripcion() != null) doctor.setDescripcion(dto.getDescripcion());
            if (dto.getEspecialidadId() != null) {
                Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Doctor no encontrado"));
        }
        doctorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Doctor eliminado correctamente"));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    @GetMapping("/usuario/{username}")
    public ResponseEntity<?> obtenerDoctorPorUsuario(@PathVariable String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Doctor doctor = doctorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("No se encontró un doctor asociado al usuario: " + username));

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/{doctorId}/horarios")
    public ResponseEntity<?> listarHorariosPorDoctor(@PathVariable Long doctorId) {
        List<Horario> horarios = horarioRepository.findByDoctorId(doctorId);
        return ResponseEntity.ok(horarios);
    }
}
