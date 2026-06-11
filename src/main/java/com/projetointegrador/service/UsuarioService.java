package com.projetointegrador.service;

import com.projetointegrador.model.Usuario;
import com.projetointegrador.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario salvar(Usuario usuario) {
        // Apenas encripta se a senha não estiver já criptografada (BCrypt começa com $2a$, $2b$ ou $2y$)
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            String senha = usuario.getSenha();
            if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                usuario.setSenha(passwordEncoder.encode(senha));
            }
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNomeCompleto(usuarioAtualizado.getNomeCompleto());
            usuario.setEmail(usuarioAtualizado.getEmail());
            if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isEmpty()) {
                String senha = usuarioAtualizado.getSenha();
                if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                    usuario.setSenha(passwordEncoder.encode(senha));
                } else {
                    usuario.setSenha(senha);
                }
            }
            usuario.setWhatsapp(usuarioAtualizado.getWhatsapp());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public void deletar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long contarUsuariosAtivos() {
        return usuarioRepository.countTotalUsuariosAtivos();
    }

    public long contar() {
        return usuarioRepository.count();
    }
}
