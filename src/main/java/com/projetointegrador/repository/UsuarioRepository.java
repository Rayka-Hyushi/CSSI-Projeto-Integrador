package com.projetointegrador.repository;

import com.projetointegrador.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    @Query("select count(distinct u.id) as total_usuarios from Usuario u left join Cliente c on u.id = c.id left join Prestador p on u.id = p.id where u.tipoUsuario != 'ROLE_ADMIN' and (c.statusAprovacao = 'APROVADO' or p.statusAprovacao = 'APROVADO')")
    int countTotalUsuariosAtivos();

    @Modifying
    @Query(value = "DELETE FROM favoritos WHERE id_prestador = :id", nativeQuery = true)
    void deleteFavoritosByPrestadorId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM favoritos WHERE id_cliente = :id", nativeQuery = true)
    void deleteFavoritosByClienteId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM prestador_servico WHERE id_prestador = :id", nativeQuery = true)
    void deletePrestadorServicoByPrestadorId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM prestador_bairros WHERE id_prestador = :id", nativeQuery = true)
    void deletePrestadorBairrosByPrestadorId(@Param("id") Long id);
}
