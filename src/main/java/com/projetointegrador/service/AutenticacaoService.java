package com.projetointegrador.service;

import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.model.Usuario;
import com.projetointegrador.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        boolean aprovado = true;

        if (usuario instanceof Cliente cliente) {
            aprovado = cliente.getStatusAprovacao() == StatusAprovacao.APROVADO;
        }

        if (usuario instanceof Prestador prestador) {
            aprovado = prestador.getStatusAprovacao() == StatusAprovacao.APROVADO;
        }

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .disabled(usuario instanceof Prestador ? false : !aprovado)
                .authorities(usuario.getTipoUsuario().name())
                .build();
    }
}
