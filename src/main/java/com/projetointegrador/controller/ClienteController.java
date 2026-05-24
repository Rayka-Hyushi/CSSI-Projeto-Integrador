package com.projetointegrador.controller;

import org.springframework.transaction.annotation.Transactional;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.service.BairroService;
import com.projetointegrador.service.ServicoAdicionalService;
import com.projetointegrador.service.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private BairroService bairroService;

    @Autowired
    private ServicoAdicionalService servicoAdicionalService;

    @Autowired
    private PrestadorService prestadorService;

    @GetMapping("/inicio")
    @Transactional(readOnly = true)
    public String homeCliente(
            @RequestParam(value = "origem", required = false) Long origem,
            @RequestParam(value = "destino", required = false) Long destino,
            @RequestParam(value = "tamanhoFrete", required = false) String tamanhoFrete,
            @RequestParam(value = "servicos", required = false) List<Long> servicosIds,
            Model model) {

        model.addAttribute("bairros", bairroService.listarTodos());
        model.addAttribute("servicos", servicoAdicionalService.listarTodos());

        List<Prestador> prestadores = prestadorService.buscarPorStatus(StatusAprovacao.APROVADO);
        List<Prestador> prestadoresFiltrados = prestadores.stream()
                .filter(prestador -> matchesBairro(prestador, origem))
                .filter(prestador -> matchesBairro(prestador, destino))
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
        return "cliente/inicio";
    }

    private boolean matchesBairro(Prestador prestador, Long bairroId) {
        if (bairroId == null) {
            return true;
        }
        return prestador.getBairros() != null && prestador.getBairros().stream()
                .anyMatch(bairro -> Objects.equals(bairro.getId(), bairroId));
    }

    private boolean matchesServicos(Prestador prestador, List<Long> servicosIds) {
        if (servicosIds == null || servicosIds.isEmpty()) {
            return true;
        }
        return prestador.getServicos() != null && servicosIds.stream()
                .allMatch(servicoId -> prestador.getServicos().stream()
                        .anyMatch(servico -> Objects.equals(servico.getId(), servicoId)));
    }
}
