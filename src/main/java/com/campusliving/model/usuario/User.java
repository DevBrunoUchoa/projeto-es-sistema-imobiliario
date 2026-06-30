package com.campusliving.model.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
@Data
@Builder
public class User {

    enum Tipo{
        Estudante,
        Locador,
        Misto,
        Admin
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("email")
    @Column(nullable = false)
    private String email;

    @JsonProperty("senhaHash")
    @Column(nullable = false)
    private String senhaHash;

    @JsonProperty("tipoConta")
    @Column(nullable = false)
    private Tipo tipoConta;

    @JsonProperty("telefone")
    @Column(nullable = false)
    private String telefone;
 
    @JsonProperty("bio")
    @Column(nullable = false)
    private String bio;   
 
    @JsonProperty("verificado")
    @Column(nullable = false)
    private boolean verificado;   
 
    @JsonProperty("ativo")
    @Column(nullable = false)
    private boolean ativo;   
 }
