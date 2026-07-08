package com.campusliving.controller.usuario;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.dto.interacao.FavoritoResponseDTO;
import com.campusliving.dto.usuario.UserPostPutRequestDTO;
import com.campusliving.dto.usuario.UserUpdateRequestDTO;
import com.campusliving.service.usuario.UserService;

import jakarta.validation.Valid;

/**
 * Base renomeada de "/user" (singular) para "/usuarios" (plural) para ficar
 * consistente com o resto das rotas do backlog do T5 (ex.: /imoveis,
 * /anuncios, /interesses) — o "/user" era um placeholder herdado da branch
 * "usuario" inicial.
 *
 * <p><b>Sobre o header {@code X-User-Id}</b>: os endpoints abaixo que exigem
 * "o próprio usuário ou ADMIN" (RF-06, RF-08/09, RF-18, RF-26/27, LGPD)
 * precisam saber quem está fazendo a requisição. Como o T5.3 (login/JWT)
 * ainda não existe neste repositório, usamos por enquanto um header simples
 * enviado pelo cliente para indicar o id do usuário autenticado. Isso é
 * DELIBERADAMENTE temporário e NÃO é seguro sozinho (qualquer cliente pode
 * mandar o header que quiser) — é só um placeholder para poder implementar e
 * testar a REGRA de autorização (dono-ou-admin) sem bloquear o T5.4 esperando
 * o T5.3. Quando o T5.3 existir, troque a origem desse UUID para o
 * SecurityContext (usuário extraído do JWT) e remova o header.</p>
 */
@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService service){
        this.userService = service;
    }

    @PostMapping()
    public ResponseEntity<?> criarUsuario(
            @RequestBody @Valid UserPostPutRequestDTO usuarioPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.criar(usuarioPostPutRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsers(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.listUsers());
    }

    // --- T5.4.1: RF-06 -----------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarPerfil(
            @PathVariable UUID id,
            @RequestBody @Valid UserUpdateRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.atualizarPerfil(id, dto, requesterId));
    }

    // --- T5.4.2: RF-07 / RNF-LEG-03 ----------------------------------------
    @GetMapping("/{id}/publico")
    public ResponseEntity<?> getPerfilPublico(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getPerfilPublico(id, requesterId));
    }

    // --- T5.4.3: RF-08 / RF-09 ----------------------------------------------
    @PostMapping(value = "/{id}/verificacao", consumes = "multipart/form-data")
    public ResponseEntity<?> solicitarVerificacao(
            @PathVariable UUID id,
            @RequestPart("documento") MultipartFile documento,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.solicitarVerificacao(id, documento, requesterId));
    }

    // --- T5.4.4: RF-18 ------------------------------------------------------
    @PatchMapping("/{id}/conta-mista")
    public ResponseEntity<?> promoverContaMista(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        userService.promoverContaMista(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    // --- T5.4.6: RF-26 / RF-27 -----------------------------------------------
    @PostMapping("/{id}/favoritos")
    public ResponseEntity<?> adicionarFavorito(
            @PathVariable UUID id,
            @RequestParam UUID adId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        FavoritoResponseDTO favorito = userService.adicionarFavorito(id, adId, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
    }

    @GetMapping("/{id}/favoritos")
    public ResponseEntity<?> listarFavoritos(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        List<FavoritoResponseDTO> favoritos = userService.listarFavoritos(id, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(favoritos);
    }

    @DeleteMapping("/{id}/favoritos/{adId}")
    public ResponseEntity<?> removerFavorito(
            @PathVariable UUID id,
            @PathVariable UUID adId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        userService.removerFavorito(id, adId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // --- T5.4.7: RNF/LEG-02 (LGPD) -------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirUsuario(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        userService.excluirUsuario(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}
