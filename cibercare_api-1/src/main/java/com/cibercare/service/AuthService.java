package com.cibercare.service;

import java.util.List;

import com.cibercare.model.Usuario;

public interface AuthService {

    String registro(Usuario usuario);

    String login(String username, String password);

    List<Usuario> listarUsuarios();

    Usuario findByUsername(String username);
}
