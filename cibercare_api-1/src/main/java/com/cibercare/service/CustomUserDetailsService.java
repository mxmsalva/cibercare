	package com.cibercare.service;
	
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.security.core.authority.SimpleGrantedAuthority;
	import org.springframework.security.core.userdetails.User;
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.security.core.userdetails.UserDetailsService;
	import org.springframework.security.core.userdetails.UsernameNotFoundException;
	import org.springframework.stereotype.Service;
	
	import com.cibercare.model.Usuario;
	import com.cibercare.repository.IUsuarioRepository;
	
	import java.util.List;
	
	@Service
	public class CustomUserDetailsService implements UserDetailsService {

	    @Autowired
	    private IUsuarioRepository usuarioRepository;

	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Usuario usuario = usuarioRepository.findByUsername(username)
	                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

	        String nombreRol = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";

	        List<SimpleGrantedAuthority> authorities = List.of(
	                new SimpleGrantedAuthority("ROLE_" + nombreRol.toUpperCase())
	        );

	        return new User(
	                usuario.getUsername(),
	                usuario.getPassword(),
	                authorities
	        );
	    }
	}
