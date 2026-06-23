package com.projetointegrador.service;

import com.projetointegrador.exceptions.SizeExceededException;
import com.projetointegrador.model.TipoUsuario;
import com.projetointegrador.model.Usuario;
import com.projetointegrador.repository.ClienteRepository;
import com.projetointegrador.repository.PrestadorRepository;
import com.projetointegrador.repository.RecomendacaoRepository;
import com.projetointegrador.repository.SolicitacaoRepository;
import com.projetointegrador.repository.UsuarioRepository;
import com.projetointegrador.repository.VeiculoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PrestadorRepository prestadorRepository;

	@Autowired
	private SolicitacaoRepository solicitacaoRepository;

	@Autowired
	private RecomendacaoRepository recomendacaoRepository;

	@Autowired
	private VeiculoRepository veiculoRepository;

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
		// Normaliza CPF para comparação e armazenamento
		String cpfFormatado = formatarCpf(usuario.getCpf());
		usuario.setCpf(cpfFormatado);

		// Normaliza WhatsApp para armazenamento
		String whatsappFormatado = formatarWhatsapp(usuario.getWhatsapp());
		usuario.setWhatsapp(whatsappFormatado);

		// 1. Validar se o e-mail já está em uso por outro usuário
		Optional<Usuario> usuarioPorEmail = buscarPorEmail(usuario.getEmail());
		if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(usuario.getId())) {
			throw new RuntimeException("Email já em uso por outro usuário");
		}

		// 2. Validar se o CPF já está em uso por outro usuário (normalizado)
		String cpfDigitos = cpfDigitos(usuario.getCpf());
		Optional<Usuario> usuarioPorCpf = buscarPorCpf(usuario.getCpf());
		if (usuarioPorCpf.isPresent() && !usuarioPorCpf.get().getId().equals(usuario.getId())) {
			throw new RuntimeException("CPF já em uso por outro usuário");
		}
		// Também verifica por CPF sem formatação (dados antigos no banco)
		List<Usuario> todos = usuarioRepository.findAll();
		for (Usuario u : todos) {
			if (!u.getId().equals(usuario.getId()) && cpfDigitos.equals(cpfDigitos(u.getCpf()))) {
				throw new RuntimeException("CPF já em uso por outro usuário");
			}
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

	@Transactional
	public void deletar(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

		if (usuario.getTipoUsuario().equals(TipoUsuario.ROLE_ADMIN)) {
			throw new RuntimeException("Administradores não podem ser deletados.");
		}

		// Remove todas as referencias vinculadas ao usuário (FK constraint)
		solicitacaoRepository.deleteByUsuarioId(id);

		if (usuario.getTipoUsuario().equals(TipoUsuario.ROLE_CLIENTE)) {
			recomendacaoRepository.deleteByClienteId(id);
			usuarioRepository.deleteFavoritosByClienteId(id);
			clienteRepository.deleteById(id);
		} else {
			recomendacaoRepository.deleteByPrestadorId(id);
			veiculoRepository.deleteByPrestadorId(id);
			usuarioRepository.deleteFavoritosByPrestadorId(id);
			usuarioRepository.deletePrestadorServicoByPrestadorId(id);
			usuarioRepository.deletePrestadorBairrosByPrestadorId(id);
			prestadorRepository.deleteById(id);
		}
		usuarioRepository.deleteById(id);
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

	private static String cpfDigitos(String cpf) {
		if (cpf == null)
			return "";
		return cpf.replaceAll("\\D+", "");
	}

	private static String formatarCpf(String cpf) {
		String digitos = cpfDigitos(cpf);
		if (digitos.length() != 11)
			return digitos;
		return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-"
				+ digitos.substring(9);
	}

	private static String formatarWhatsapp(String whatsapp) {
		if (whatsapp == null)
			return "";
		String digitos = whatsapp.replaceAll("\\D+", "");
		if (digitos.length() > 11 && digitos.startsWith("55")) {
			digitos = digitos.substring(2);
		}
		digitos = digitos.substring(0, Math.min(digitos.length(), 11));
		if (digitos.length() == 11) {
			return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
		} else if (digitos.length() == 10) {
			return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
		}
		return digitos;
	}
}
