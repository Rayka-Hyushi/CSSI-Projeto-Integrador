package com.projetointegrador.controller;

import org.springframework.transaction.annotation.Transactional;
import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.model.TipoVeiculo;
import com.projetointegrador.service.BairroService;
import com.projetointegrador.service.ClienteService;
import com.projetointegrador.service.ServicoAdicionalService;
import com.projetointegrador.service.PrestadorService;
import com.projetointegrador.service.RecomendacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private BairroService bairroService;

    @Autowired
    private ServicoAdicionalService servicoAdicionalService;

    @Autowired
    private PrestadorService prestadorService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private RecomendacaoService recomendacaoService;

    @GetMapping("/inicio")
    @Transactional(readOnly = true)
    public String homeCliente(
            @RequestParam(value = "origem", required = false) Long origem,
            @RequestParam(value = "destino", required = false) Long destino,
            @RequestParam(value = "tamanhoFrete", required = false) String tamanhoFrete,
            @RequestParam(value = "servicos", required = false) List<Long> servicosIds,
            Authentication authentication,
            Model model) {

        model.addAttribute("bairros", bairroService.listarTodos());
        model.addAttribute("servicos", servicoAdicionalService.listarTodos());

        List<Prestador> prestadores = prestadorService.buscarPorStatus(StatusAprovacao.APROVADO);
        List<Prestador> prestadoresFiltrados = prestadores.stream()
                .filter(prestador -> matchesBairro(prestador, origem))
                .filter(prestador -> matchesBairro(prestador, destino))
                .filter(prestador -> matchesTamanhoFrete(prestador, tamanhoFrete))
                .filter(prestador -> matchesServicos(prestador, servicosIds))
                .sorted(Comparator.comparing(Prestador::getNomeCompleto, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        model.addAttribute("prestadores", prestadoresFiltrados);
        model.addAttribute("totalPrestadores", prestadoresFiltrados.size());
        model.addAttribute("buscaRealizada", origem != null || destino != null || (tamanhoFrete != null && !tamanhoFrete.isBlank()) || (servicosIds != null && !servicosIds.isEmpty()));
        model.addAttribute("origemSelecionada", origem);
        model.addAttribute("destinoSelecionado", destino);
        model.addAttribute("tamanhoFreteSelecionado", tamanhoFrete);
        model.addAttribute("servicosSelecionados", servicosIds != null ? servicosIds : new ArrayList<>());

        // Adiciona o usuário logado ao modelo e lista de favoritos
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                List<Long> favoritosIds = cliente.getPrestadores().stream()
                        .map(Prestador::getId)
                        .toList();
                model.addAttribute("favoritosIds", favoritosIds);
            }
            model.addAttribute("usuarioLogadoId", email);
        }

        return "cliente/inicio";
    }

    @GetMapping("/favoritos")
    @Transactional(readOnly = true)
    public String listarFavoritos(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                // Inicializa a coleção lazy
                if (cliente.getPrestadores() != null) {
                    cliente.getPrestadores().size();
                }
                model.addAttribute("favoritos", cliente.getPrestadores());
            }
        }
        return "cliente/favoritos";
    }

    @PostMapping("/favoritar/{prestadorId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> alternarFavorito(
            @PathVariable Long prestadorId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("sucesso", false, "mensagem", "Usuário não autenticado"));
        }

        try {
            String email = authentication.getName();
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
            Optional<Prestador> prestadorOpt = prestadorService.buscarPorId(prestadorId);

            if (clienteOpt.isEmpty() || prestadorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem", "Cliente ou Prestador não encontrado"));
            }

            Cliente cliente = clienteOpt.get();
            Prestador prestador = prestadorOpt.get();
            List<Prestador> favoritos = cliente.getPrestadores();
            
            boolean isFavorito;
            if (favoritos.contains(prestador)) {
                favoritos.remove(prestador);
                isFavorito = false;
            } else {
                favoritos.add(prestador);
                isFavorito = true;
            }
            
            clienteService.salvar(cliente);
            return ResponseEntity.ok(Map.of("sucesso", true, "isFavorito", isFavorito, "mensagem", isFavorito ? "Adicionado aos favoritos" : "Removido dos favoritos"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("sucesso", false, "mensagem", "Erro ao processar favorito: " + e.getMessage()));
        }
    }

    @PostMapping("/recomendar/{prestadorId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> recomendarPrestador(
            @PathVariable Long prestadorId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new RespostaRecomendacao(false, "Usuário não autenticado"));
        }

        try {
            // Busca o cliente logado pelo email (principal)
            String email = authentication.getName();
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);

            if (clienteOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new RespostaRecomendacao(false, "Cliente não encontrado"));
            }

            Cliente cliente = clienteOpt.get();
            boolean recomendacaoRegistrada = recomendacaoService.recomendarPrestador(cliente, prestadorId);

            if (recomendacaoRegistrada) {
                // Busca o prestador atualizado
                Prestador prestador = prestadorService.buscarPorId(prestadorId).orElse(null);
                int novasRecomendacoes = prestador != null ? prestador.getRecomendacoes() : 0;
                return ResponseEntity.ok(new RespostaRecomendacao(true, "Recomendação registrada com sucesso!", novasRecomendacoes));
            } else {
                return ResponseEntity.badRequest().body(new RespostaRecomendacao(false, "Você já recomendou este prestador"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new RespostaRecomendacao(false, "Erro ao registrar recomendação: " + e.getMessage()));
        }
    }

    private boolean matchesBairro(Prestador prestador, Long bairroId) {
        if (bairroId == null) {
            return true;
        }
        return prestador.getBairros() != null && prestador.getBairros().stream()
                .anyMatch(bairro -> Objects.equals(bairro.getId(), bairroId));
    }

    private boolean matchesTamanhoFrete(Prestador prestador, String tamanhoFrete) {
        if (tamanhoFrete == null || tamanhoFrete.isBlank()) {
            return true;
        }

        List<TipoVeiculo> tiposValidos = getTiposVeiculosPorTamanhoFrete(tamanhoFrete);

        return prestador.getVeiculos() != null && prestador.getVeiculos().stream()
                .anyMatch(veiculo -> tiposValidos.contains(veiculo.getTipo()));
    }

    private List<TipoVeiculo> getTiposVeiculosPorTamanhoFrete(String tamanhoFrete) {
        return switch (tamanhoFrete) {
            case "PEQUENO" -> Arrays.asList(TipoVeiculo.FIORINO, TipoVeiculo.VAN);
            case "MEDIO" -> Arrays.asList(TipoVeiculo.CAMINHONETE, TipoVeiculo.VUC);
            case "GRANDE" -> Arrays.asList(TipoVeiculo.BAU_ABERTO, TipoVeiculo.BAU_FECHADO);
            default -> new ArrayList<>();
        };
    }

    private boolean matchesServicos(Prestador prestador, List<Long> servicosIds) {
        if (servicosIds == null || servicosIds.isEmpty()) {
            return true;
        }
        return prestador.getServicos() != null && servicosIds.stream()
                .allMatch(servicoId -> prestador.getServicos().stream()
                        .anyMatch(servico -> Objects.equals(servico.getId(), servicoId)));
    }

    // Classe auxiliar para respostas AJAX
    public static class RespostaRecomendacao {
        public boolean sucesso;
        public String mensagem;
        public int novasRecomendacoes;

        public RespostaRecomendacao(boolean sucesso, String mensagem) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            this.novasRecomendacoes = 0;
        }

        public RespostaRecomendacao(boolean sucesso, String mensagem, int novasRecomendacoes) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            this.novasRecomendacoes = novasRecomendacoes;
        }

        // Getters
        public boolean isSucesso() {
            return sucesso;
        }

        public String getMensagem() {
            return mensagem;
        }

        public int getNovasRecomendacoes() {
            return novasRecomendacoes;
        }
    }
}
