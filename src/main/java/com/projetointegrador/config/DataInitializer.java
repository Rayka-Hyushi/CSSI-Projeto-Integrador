package com.projetointegrador.config;

import com.projetointegrador.model.Bairro;
import com.projetointegrador.model.ServicoAdicional;
import com.projetointegrador.repository.BairroRepository;
import com.projetointegrador.repository.ServicoAdicionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Inicializador de dados que popula tabelas de bairros e serviços adicionais
 * na primeira inicialização da aplicação.
 */
@Component
public class DataInitializer {

	@Autowired
	private BairroRepository bairroRepository;

	@Autowired
	private ServicoAdicionalRepository servicoAdicionalRepository;

	@PostConstruct
	public void init() {
		inicializarBairros();
		inicializarServicosAdicionais();
	}

	private void inicializarBairros() {
		if (bairroRepository.count() == 0) {
			String[] bairros = {
					// Bairros Urbanos (42)
					"Agroindustrial", "Boi Morto", "Bonfim", "Camobi", "Campestre do Menino Deus",
					"Carolina", "Caturrita", "Centro", "Cerrito", "Chácara das Flores",
					"Divina Providência", "Dom Antônio Reis", "Duque de Caxias", "Itararé", "João Luiz Pozzobon",
					"Juscelino Kubitschek", "Km Três", "Lorenzi", "Menino Jesus", "Noal",
					"Nonoai", "Nossa Senhora das Dores", "Nossa Senhora de Fátima", "Nossa Senhora de Lourdes",
					"Nossa Senhora do Perpétuo Socorro", "Nossa Senhora do Rosário", "Nossa Senhora Medianeira",
					"Nova Santa Marta", "Passo d'Areia", "Passo das Tropas", "Patronato", "Pé de Plátano",
					"Pinheiro Machado", "Presidente João Goulart", "Renascença", "Salgado Filho",
					"São João", "São José", "Tancredo Neves", "Tomazetti", "Uglione", "Urlândia",
					// Distritos Rurais (9)
					"Arroio do Só", "Arroio Grande", "Boca do Monte", "Palma", "Pains",
					"Passo do Verde", "Santa Flora", "Santo Antão", "São Valentim"
			};

			for (String nomeBairro : bairros) {
				Bairro b = new Bairro();
				b.setNomeBairro(nomeBairro);
				bairroRepository.save(b);
			}

			System.out.println("✓ Bairros inicializados com sucesso!");
		}
	}

	private void inicializarServicosAdicionais() {
		if (servicoAdicionalRepository.count() == 0) {
			String[] servicos = {
					"Ajudante para Carga e Descarga",
					"Montagem/Desmontagem de Móveis",
					"Embalagem de Itens",
					"Transporte de Itens Especiais"
			};

			for (String nomeServico : servicos) {
				ServicoAdicional s = new ServicoAdicional();
				s.setNomeServico(nomeServico);
				servicoAdicionalRepository.save(s);
			}

			System.out.println("✓ Serviços adicionais inicializados com sucesso!");
		}
	}
}



