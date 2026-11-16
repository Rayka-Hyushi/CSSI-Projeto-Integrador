package com.projetointegrador.service;

import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.Prestador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioServiceTest {

    @Test
    @DisplayName("Deve validar CNPJ válido formatado e desformatado")
    void deveValidarCnpjValido() {
        // CNPJ válido conhecido (ex: Banco do Brasil 00.000.000/0001-91)
        assertThat(UsuarioService.validarCnpj("00.000.000/0001-91")).isTrue();
        assertThat(UsuarioService.validarCnpj("00000000000191")).isTrue();

        // Outro CNPJ válido padrão
        assertThat(UsuarioService.validarCnpj("11.222.333/0001-81")).isTrue();
        assertThat(UsuarioService.validarCnpj("11222333000181")).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ inválido")
    void deveRejeitarCnpjInvalido() {
        // Nulo ou vazio
        assertThat(UsuarioService.validarCnpj(null)).isFalse();
        assertThat(UsuarioService.validarCnpj("")).isFalse();
        assertThat(UsuarioService.validarCnpj("   ")).isFalse();

        // Dígitos repetidos
        assertThat(UsuarioService.validarCnpj("00.000.000/0000-00")).isFalse();
        assertThat(UsuarioService.validarCnpj("11.111.111/1111-11")).isFalse();

        // Tamanho incorreto
        assertThat(UsuarioService.validarCnpj("123.456.789-00")).isFalse();
        assertThat(UsuarioService.validarCnpj("1122233300018")).isFalse();

        // Dígitos verificadores incorretos
        assertThat(UsuarioService.validarCnpj("11.222.333/0001-82")).isFalse();
        assertThat(UsuarioService.validarCnpj("00.000.000/0001-92")).isFalse();
    }

    @Test
    @DisplayName("Deve formatar CNPJ corretamente")
    void deveFormatarCnpj() {
        assertThat(UsuarioService.formatarCnpj("00000000000191")).isEqualTo("00.000.000/0001-91");
        assertThat(UsuarioService.formatarCnpj("00.000.000/0001-91")).isEqualTo("00.000.000/0001-91");
        assertThat(UsuarioService.formatarCnpj("11222333000181")).isEqualTo("11.222.333/0001-81");
        assertThat(UsuarioService.formatarCnpj(null)).isNull();
        assertThat(UsuarioService.formatarCnpj("")).isNull();
    }

    @Test
    @DisplayName("Deve extrair apenas dígitos do CNPJ")
    void deveExtrairDigitosCnpj() {
        assertThat(UsuarioService.cnpjDigitos("11.222.333/0001-81")).isEqualTo("11222333000181");
        assertThat(UsuarioService.cnpjDigitos(null)).isEqualTo("");
    }

    @Test
    @DisplayName("Cliente e Prestador devem herdar campo CNPJ")
    void deveHerdarCnpj() {
        Cliente cliente = new Cliente();
        cliente.setCnpj("00.000.000/0001-91");
        assertThat(cliente.getCnpj()).isEqualTo("00.000.000/0001-91");

        Prestador prestador = new Prestador();
        prestador.setCnpj("11.222.333/0001-81");
        assertThat(prestador.getCnpj()).isEqualTo("11.222.333/0001-81");
    }
}
