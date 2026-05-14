package com.projetointegrador.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pode não haver um usuário caso seja um Cadastro ou link de recuperação
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Campos temporários usados para Cadastro/Recuperação onde o usuário final não está gerado ou é apenas referência cruzada
    @Column(length = 150)
    private String nome;

    @Column(length = 150)
    private String email;

    @Column(length = 17)
    private String whatsapp;

    @Column(length = 14)
    private String cpf;

    // Armazena JSON ou String delimitada se for dados de Cadastro de Prestador, etc. (Opcional, focado em suporte por enquanto)
    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAprovacao status = StatusAprovacao.PENDENTE;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @PrePersist
    protected void onCreate() {
        this.dataSolicitacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public TipoSolicitacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoSolicitacao tipo) {
        this.tipo = tipo;
    }

    public StatusAprovacao getStatus() {
        return status;
    }

    public void setStatus(StatusAprovacao status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
}
