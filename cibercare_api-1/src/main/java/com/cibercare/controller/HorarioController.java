package com.cibercare.controller;

import com.cibercare.model.Doctor;
import com.cibercare.model.Horario;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IHorarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    @Autowired
    private IHorarioRepository horarioRepository;

    @Autowired
    private IDoctorRepository doctorRepository;

    // =============================
    // 🔹 ADMIN: Ver todos los horarios
    // =============================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Horario> listarHorarios() {
        return horarioRepository.findAll();
    }

    // =============================
    // 🔹 DOCTOR: Ver solo sus horarios
    // =============================
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/mis-horarios")
    public ResponseEntity<?> listarHorariosDelDoctor(Authentication auth) {
        String username = auth.getName();

        Doctor doctor = doctorRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado para el usuario autenticado"));

        List<Horario> horarios = horarioRepository.findByDoctorId(doctor.getId());
        return ResponseEntity.ok(horarios);
    }

    // =============================
    // 🔹 ADMIN o DOCTOR: Ver horarios de un doctor específico (por ID)
    // =============================
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/doctor/{doctorId}")
    public List<Horario> listarHorariosPorDoctor(@PathVariable Long doctorId) {
        return horarioRepository.findByDoctorId(doctorId);
    }

    // =============================
    // 🔹 DOCTOR: Crear horario propio
    // =============================
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PostMapping
    public ResponseEntity<?> crearHorario(@RequestBody Horario horario, Authentication auth) {
        try {
            String username = auth.getName();
            Horario nuevoHorario;

            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
                // 🔹 Si es un doctor, asignar su propio usuario
                Doctor doctor = doctorRepository.findByUsuarioUsername(username)
                        .orElseThrow(() -> new RuntimeException("Doctor no encontrado para el usuario autenticado"));
                horario.setDoctor(doctor);
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                // 🔹 Si es admin, debe venir un doctor.id en el body
                if (horario.getDoctor() == null || horario.getDoctor().getId() == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar el ID del doctor"));
                }

                Doctor doctor = doctorRepository.findById(horario.getDoctor().getId())
                        .orElseThrow(() -> new RuntimeException("Doctor no encontrado con ID: " + horario.getDoctor().getId()));
                horario.setDoctor(doctor);
            }

            nuevoHorario = horarioRepository.save(horario);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Horario creado correctamente",
                    "horario", nuevoHorario
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear horario: " + e.getMessage()));
        }
    }


    // =============================
    // 🔹 ADMIN o DOCTOR: Cambiar disponibilidad
    // =============================
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<?> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        Horario horario = horarioRepository.findById(id)
                .orElse(null);

        if (horario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Horario no encontrado"));
        }

        horario.setDisponible(disponible);
        horarioRepository.save(horario);
        return ResponseEntity.ok(horario);
    }

    // =============================
    // 🔹 DOCTOR: Eliminar solo su horario
    // =============================
  
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarHorario(@PathVariable Long id) {
        try {
            if (!horarioRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Horario no encontrado"));
            }

            horarioRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Horario eliminado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el horario"));
        }
    }
}
