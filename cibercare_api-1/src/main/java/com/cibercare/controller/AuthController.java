package com.cibercare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.cibercare.dto.LoginDTO;
import com.cibercare.model.Doctor;
import com.cibercare.model.Paciente;
import com.cibercare.model.Rol;
import com.cibercare.model.Usuario;
import com.cibercare.repository.IDoctorRepository;
import com.cibercare.repository.IPacienteRepository;
import com.cibercare.repository.IRolRepository;
import com.cibercare.repository.IUsuarioRepository;
import com.cibercare.security.JwtUtils;
import com.cibercare.service.AuthService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IDoctorRepository doctorRepository;

	@Autowired
	private IPacienteRepository pacienteRepository;

	@Autowired
	private IRolRepository rolRepository;
	
	@Autowired
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


	private final AuthService authService;
	private final JwtUtils jwtUtils;

	
	@PostMapping("/register")
	public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, Object> request) {
	    try {
	        Usuario usuario = new Usuario();
	        usuario.setNombreCompleto((String) request.get("nombreCompleto"));
	        usuario.setEmail((String) request.get("email"));
	        usuario.setUsername((String) request.get("username"));
	        usuario.setPassword(passwordEncoder.encode((String) request.get("password")));
	        Object rolObj = request.get("rol");
	        Rol rol = null;
	        if (rolObj instanceof Map<?, ?> rolMap && rolMap.containsKey("id")) {
	            Long rolId = ((Number) rolMap.get("id")).longValue();
	            rol = rolRepository.findById(rolId).orElse(null);
	        }
	        if (rol == null) {
	            return ResponseEntity.badRequest().body(Map.of("error", "Rol no encontrado"));
	        }
	        usuario.setRol(rol);
	        usuarioRepository.save(usuario);
	        if ("DOCTOR".equalsIgnoreCase(rol.getNombre())) {
	            Doctor doctor = new Doctor();
	            doctor.setUsuario(usuario); 
	            doctorRepository.save(doctor);
	        }
	        if ("PACIENTE".equalsIgnoreCase(rol.getNombre())) {
	            Paciente paciente = new Paciente();
	            paciente.setUsuario(usuario);
	            pacienteRepository.save(paciente);
	        }
	        return ResponseEntity.ok(Map.of(
	                "mensaje", "Usuario registrado correctamente",
	                "usuario", usuario
	        ));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError()
	                .body(Map.of("error", "Error al registrar usuario: " + e.getMessage()));
	    }
	}

	@PostMapping("/register/pacientes")
	public ResponseEntity<?> registrarPaciente(@RequestBody Map<String, Object> request) {
	    try {
	        Usuario usuario = new Usuario();
	        usuario.setNombreCompleto((String) request.get("nombreCompleto"));
	        usuario.setEmail((String) request.get("email"));
	        usuario.setUsername((String) request.get("username"));
	        usuario.setPassword(passwordEncoder.encode((String) request.get("password")));

	        Rol rolPaciente = rolRepository.findByNombre("PACIENTE")
	                .orElseThrow(() -> new RuntimeException("Rol 'PACIENTE' no encontrado"));
	        usuario.setRol(rolPaciente);

	        Usuario nuevoUsuario = usuarioRepository.save(usuario);

	        Paciente paciente = new Paciente();
	        paciente.setUsuario(nuevoUsuario);
	        paciente.setDireccion((String) request.get("direccion"));
	        paciente.setTelefono((String) request.get("telefono"));

	        Object fechaNacObj = request.get("fechaNacimiento");
	        if (fechaNacObj != null) {
	            paciente.setFechaNacimiento(LocalDate.parse(fechaNacObj.toString()));
	        }
	        pacienteRepository.save(paciente);
	        return ResponseEntity.ok(Map.of(
	                "mensaje", "Paciente registrado correctamente",
	                "usuarioId", nuevoUsuario.getId(),
	                "pacienteId", paciente.getId()
	        ));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Error al registrar paciente: " + e.getMessage()));
	    }
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
			UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
			Usuario usuario = authService.findByUsername(request.getUsername());
			String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";
			String token = jwtUtils.generateToken(userDetails, rolNombre);
			return ResponseEntity.ok(Map.of("token", token));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
		}
	}

	@GetMapping("/validate")
	public ResponseEntity<?> validate(@RequestParam String token) {
		try {
			String username = jwtUtils.extractUsername(token);
			boolean valid = jwtUtils.isTokenValid(token, userDetailsService.loadUserByUsername(username));

			Map<String, Object> claims = Map.of("username", username, "valid", valid);

			return ResponseEntity.ok(claims);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("valid", false, "error", "Token inválido o expirado"));
		}
	}
	
	

	@GetMapping("/usuarios")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> listarUsuarios() {
	    List<Usuario> usuarios = authService.listarUsuarios();
	    return ResponseEntity.ok(usuarios);
	}

	
	@DeleteMapping("/usuarios/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
	    try {
	        if (!usuarioRepository.existsById(id)) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("error", "Usuario no encontrado"));
	        }

	        usuarioRepository.deleteById(id);
	        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Error al eliminar el usuario"));
	    }
	}

}