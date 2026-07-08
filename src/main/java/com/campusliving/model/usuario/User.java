package com.campusliving.model.usuario;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeia a tabela {@code users} (ver
 * {@code src/main/resources/db/migration/V2__create_users.sql}).
 *
 * <p>Os nomes de propriedade Java ({@code nome}, {@code verificado}...) foram
 * mantidos como já estavam no restante do código (controller/service/DTO) para
 * não quebrar o que já existia; o que muda de nome é só a coluna física no
 * banco, via {@code @Column(name = "...")}. Isso é proposital: o schema (nomes
 * de tabela/coluna) é decidido pelo diagrama ER do time e não deveria mudar
 * conforme a gente refatora Java.</p>
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /** Precisa bater exatamente com o CHECK constraint de users.tipo_conta. */
    public enum Tipo {
        ESTUDANTE,
        LOCADOR,
        MISTO,
        ADMIN
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("nome")
    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nome;

    @JsonProperty("email")
    @Column(nullable = false, length = 180)
    private String email;

    @JsonProperty("senhaHash")
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @JsonProperty("tipoConta")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private Tipo tipoConta;

    @JsonProperty("telefone")
    @Column(length = 20)
    private String telefone;

    @JsonProperty("fotoUrl")
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @JsonProperty("bio")
    @Column
    private String bio;

    // RF-06: atualizáveis via PUT /usuarios/:id. Adicionados em
    // V18__add_curso_instituicao_users.sql — não estavam no ER original.
    @JsonProperty("curso")
    @Column(length = 150)
    private String curso;

    @JsonProperty("instituicao")
    @Column(length = 150)
    private String instituicao;

    @JsonProperty("verificado")
    @Column(name = "email_verificado", nullable = false)
    private boolean verificado;

    @JsonProperty("ativo")
    @Column(nullable = false)
    private boolean ativo;

    // RNF/LEG-01 (LGPD): consentimento explícito no cadastro. Ainda não é
    // preenchido por nenhum endpoint (isso é T5.3), mas a coluna já existe no
    // banco desde já — mapeamos aqui para não ficar "invisível" pro JPA.
    @JsonProperty("aceiteLgpd")
    @Column(name = "aceite_lgpd", nullable = false)
    private boolean aceiteLgpd;

    @JsonProperty("aceiteLgpdData")
    @Column(name = "aceite_lgpd_data")
    private OffsetDateTime aceiteLgpdData;

    // Preenchidos pelo banco (DEFAULT now() / trigger) — a aplicação nunca
    // escreve nessas colunas, por isso insertable/updatable = false.
    // Atenção: como o Hibernate não faz SELECT de volta depois do INSERT/UPDATE
    // por padrão, esses dois campos ficam null no objeto que você acabou de
    // salvar em memória — só vêm preenchidos ao reler do banco
    // (ex.: repository.findById(id) depois do save()).
    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @JsonProperty("dataAtualizacao")
    @Column(name = "data_atualizacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataAtualizacao;

    @JsonProperty("ultimoLogin")
    @Column(name = "ultimo_login")
    private OffsetDateTime ultimoLogin;

    // ===== MÉTODOS DO SPRING SECURITY (UserDetails) =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + tipoConta.name()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return verificado && ativo;
    }

    // ===== GETTERS ADICIONAIS PARA COMPATIBILIDADE =====

    public boolean getAtivo() {
        return this.ativo;
    }

    public boolean getVerificado() {
        return this.verificado;
    }

    public String getEmail() {
        return this.email;
    }

    public String getRole() {
        return tipoConta != null ? tipoConta.name() : "ESTUDANTE";
    }

    public Boolean getEmailVerificado() {
        return verificado;
    }

    public String getSenha() {
        return senhaHash;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public boolean isVerificado() {
        return this.verificado;
    }

    public String getSenhaHash() {
        return this.senhaHash;
    }

    public User.Tipo getTipoConta() {
        return this.tipoConta;
    }

}