package com.projetointegrador.service;

import com.projetointegrador.exceptions.SizeExceededException;
import com.projetointegrador.model.TipoUsuario;
import com.projetointegrador.model.Usuario;
import com.projetointegrador.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private PrestadorService prestadorService;

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

	public Optional<Usuario> buscarPorCpf(String cpf) {
		return usuarioRepository.findByCpf(cpf);
	}

	public Usuario salvar(Usuario usuario) {
		// 1. Validar se o e-mail já está em uso por outro usuário
		Optional<Usuario> usuarioPorEmail = buscarPorEmail(usuario.getEmail());
		if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(usuario.getId())) {
			throw new RuntimeException("Email já em uso por outro usuário");
		}

		// 2. Validar se o CPF já está em uso por outro usuário
		Optional<Usuario> usuarioPorCpf = buscarPorCpf(usuario.getCpf());
		if (usuarioPorCpf.isPresent() && !usuarioPorCpf.get().getId().equals(usuario.getId())) {
			throw new RuntimeException("CPF já em uso por outro usuário");
		}

		// 3. Validar foto de perfil
		if (usuario.getProfilePhotoUrl() != null && !usuario.getProfilePhotoUrl().isEmpty()) {
			String profilePhotoUrl = usuario.getProfilePhotoUrl();
			if (!profilePhotoUrl.startsWith("/")) {
				profilePhotoUrl = "/" + profilePhotoUrl;
				usuario.setProfilePhotoUrl(profilePhotoUrl);
			}

			// Validar arquivo físico na pasta local de uploads
			if (profilePhotoUrl.startsWith("/uploads/")) {
				java.nio.file.Path path = java.nio.file.Paths.get(profilePhotoUrl.substring(1));
				if (java.nio.file.Files.exists(path)) {
					try {
						// Validar formato do arquivo (extensões aceitas)
						String nomeArquivo = path.getFileName().toString().toLowerCase();
						if (!nomeArquivo.endsWith(".jpg") && !nomeArquivo.endsWith(".jpeg") &&
								!nomeArquivo.endsWith(".png") && !nomeArquivo.endsWith(".webp")
								&& !nomeArquivo.endsWith(".gif")) {
							java.nio.file.Files.deleteIfExists(path);
							throw new RuntimeException(
									"Formato de imagem inválido. Apenas JPG, JPEG, PNG, WEBP ou GIF são permitidos.");
						}

						// Validar tamanho (tamanho máximo de 2MB)
						long maxBytes = 2 * 1024 * 1024; // 2MB
						if (java.nio.file.Files.size(path) > maxBytes) {
							java.nio.file.Files.deleteIfExists(path);
							throw new SizeExceededException();
						}
					} catch (IOException e) {
						throw new RuntimeException("Erro ao validar arquivo de foto de perfil: " + e.getMessage());
					}
				}
			}
		}

		// 4. Criptografia de senha se aplicável
		if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
			String senha = usuario.getSenha();
			if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
				usuario.setSenha(passwordEncoder.encode(senha));
			}
		}

		return usuarioRepository.save(usuario);
	}

	public void deletar(Long id) {
		if (usuarioRepository.findById(id).get().getTipoUsuario().equals(TipoUsuario.ROLE_ADMIN)) {
			throw new RuntimeException("Administradores não podem ser deletados.");
		} else if (!usuarioRepository.existsById(id)) {
			throw new RuntimeException("Usuário não encontrado.");
		} else {
			if (buscarPorId(id).get().getTipoUsuario().equals(TipoUsuario.ROLE_CLIENTE)) {
				clienteService.deletar(id);
			} else {
				prestadorService.deletar(id);
			}
			usuarioRepository.deleteById(id);
		}
	}

	public boolean existePorCpf(String cpf) {
		return usuarioRepository.existsByCpf(cpf);
	}

	public boolean existePorEmail(String email) {
		return usuarioRepository.existsByEmail(email);
	}

	public long contarUsuariosAtivos() {
		return usuarioRepository.countTotalUsuariosAtivos();
	}

	public long contar() {
		return usuarioRepository.count();
	}
}
