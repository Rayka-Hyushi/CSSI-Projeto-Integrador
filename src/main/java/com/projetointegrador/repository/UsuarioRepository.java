package com.projetointegrador.repository;

import com.projetointegrador.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("select count(distinct u.id) as total_usuarios from Usuario u left join Cliente c on u.id = c.id left join Prestador p on u.id = p.id where u.tipoUsuario != 'ROLE_ADMIN' and (c.statusAprovacao = 'APROVADO' or p.statusAprovacao = 'APROVADO')")
    int countTotalUsuariosAtivos();
}
