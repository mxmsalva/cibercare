package com.cibercare.service;

import com.cibercare.model.Usuario;
import com.cibercare.repository.IUsuarioRepository;
import com.cibercare.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final IUsuarioRepository usuarioRepository;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService userDetailsService;

	@Override
	public String registro(Usuario usuario) {
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		usuarioRepository.save(usuario);
		return "Usuario registrado correctamente";
	}

	@Override
	public String login(String username, String password) {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (passwordEncoder.matches(password, usuario.getPassword())) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";

			return jwtUtils.generateToken(userDetails, rolNombre);
		}

		throw new RuntimeException("Contraseña incorrecta");
	}

	@Override
	public List<Usuario> listarUsuarios() {
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario findByUsername(String username) {
		return usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	}
}