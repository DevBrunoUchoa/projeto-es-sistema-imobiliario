package com.campusliving.service.usuario;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.dto.usuario.UserPostPutRequestDTO;
import com.campusliving.dto.usuario.UserPublicProfileDTO;
import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.dto.usuario.UserUpdateRequestDTO;
import com.campusliving.dto.usuario.VerificacaoLocadorResponseDTO;
import com.campusliving.dto.interacao.FavoritoResponseDTO;
import com.campusliving.exception.imovel.ImagemInvalidaException;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.interacao.FavoritoDuplicadoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.EmailEmUsoException;
import com.campusliving.exception.usuario.TipoContaInvalidoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.exception.usuario.VerificacaoJaPendenteException;
import com.campusliving.model.interacao.Favorito;
import com.campusliving.model.usuario.User;
import com.campusliving.model.usuario.VerificacaoLocador;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.interacao.FavoritoRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.repository.usuario.VerificacaoLocadorRepository;
import com.campusliving.service.imovel.ImageStorageService;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository repository;
    private final ModelMapper modelMapper;
    private final VerificacaoLocadorRepository verificacaoLocadorRepository;
    private final FavoritoRepository favoritoRepository;
    private final ContatoRepository contatoRepository;
    private final DocumentStorageService documentStorageService;
    private final ImageStorageService imageStorageService;

    public UserServiceImpl(
            UserRepository repository,
            ModelMapper model,
            VerificacaoLocadorRepository verificacaoLocadorRepository,
            FavoritoRepository favoritoRepository,
            ContatoRepository contatoRepository,
            DocumentStorageService documentStorageService,
            ImageStorageService imageStorageService
    ){
        this.repository = repository;
        this.modelMapper = model;
        this.verificacaoLocadorRepository = verificacaoLocadorRepository;
        this.favoritoRepository = favoritoRepository;
        this.contatoRepository = contatoRepository;
        this.documentStorageService = documentStorageService;
        this.imageStorageService = imageStorageService;
    }

	@Override
	public User getUserById(UUID userId) {
        User usuario = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        return usuario;
    }

	@Override
	public List<UserResponseDTO> listUsers() {
		List<User> usuarios = repository.findAll();
        return usuarios.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
	}

	@Override
	public UserResponseDTO criar(UserPostPutRequestDTO usuarioPostPutRequestDTO) {
        User usuario = modelMapper.map(usuarioPostPutRequestDTO, User.class);
        List<User> users = repository.findByEmail(usuario.getEmail());
        if (users.size() != 0) {
            throw new EmailEmUsoException();
        }
        repository.save(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
	}

    // -------------------------------------------------------------------
    // T5.4: gerenciamento de usuários
    // -------------------------------------------------------------------

    @Override
    public UserResponseDTO atualizarPerfil(UUID id, UserUpdateRequestDTO dto, UUID requesterId) {
        User usuario = repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDonoOuAdmin(requesterId, id);

        // Atualização parcial: só sobrescreve o que veio preenchido no corpo.
        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getBio() != null) {
            usuario.setBio(dto.getBio());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }
        if (dto.getCurso() != null) {
            usuario.setCurso(dto.getCurso());
        }
        if (dto.getInstituicao() != null) {
            usuario.setInstituicao(dto.getInstituicao());
        }
        if (dto.getRole() != null) {
            // ADMIN nunca pode ser escolhido pelo próprio usuário pela tela de perfil.
            if (dto.getRole() == User.Tipo.ADMIN) {
                throw new AcessoNegadoException();
            }
            usuario.setTipoConta(dto.getRole());
        }

        repository.save(usuario);
        return new UserResponseDTO(usuario);
    }

    private static final long MAX_FOTO_PERFIL_BYTES = 5L * 1024 * 1024; // RNF/SEG-05
    private static final List<String> TIPOS_FOTO_PERMITIDOS =
            List.of("image/jpeg", "image/png", "image/webp");

    @Override
    public UserResponseDTO atualizarFotoPerfil(UUID id, MultipartFile foto, UUID requesterId) {
        User usuario = repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDonoOuAdmin(requesterId, id);
        validarFotoPerfil(foto);

        String caminho = "perfis/" + id + "/" + UUID.randomUUID() + extensaoDe(foto.getContentType());
        ImageStorageService.StoredImage armazenada = imageStorageService.upload(caminho, foto);

        usuario.setFotoUrl(armazenada.publicUrl());
        repository.save(usuario);
        return new UserResponseDTO(usuario);
    }

    private void validarFotoPerfil(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new ImagemInvalidaException("Envie uma imagem para a foto de perfil");
        }
        if (foto.getSize() > MAX_FOTO_PERFIL_BYTES) {
            throw new ImagemInvalidaException("A foto de perfil deve ter no maximo 5 MB");
        }
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS_FOTO_PERMITIDOS.contains(tipo)) {
            throw new ImagemInvalidaException("Formato permitido: JPEG, PNG ou WEBP");
        }
    }

    private String extensaoDe(String contentType) {
        if (contentType == null) {
            return ".jpg";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    @Override
    public UserPublicProfileDTO getPerfilPublico(UUID id, UUID requesterId) {
        User alvo = repository.findById(id).orElseThrow(UserNotFoundException::new);

        boolean ehOProprio = requesterId != null && requesterId.equals(id);
        boolean ehAdmin = !ehOProprio && requesterId != null
                && repository.findById(requesterId)
                        .map(u -> u.getTipoConta() == User.Tipo.ADMIN)
                        .orElse(false);
        boolean contatoPrevio = !ehOProprio && !ehAdmin && requesterId != null
                && contatoRepository.existeContatoEntre(requesterId, id);

        boolean contatoLiberado = ehOProprio || ehAdmin || contatoPrevio;
        return UserPublicProfileDTO.of(alvo, contatoLiberado);
    }

    @Override
    public VerificacaoLocadorResponseDTO solicitarVerificacao(UUID id, MultipartFile documento, UUID requesterId) {
        User usuario = repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDonoOuAdmin(requesterId, id);

        boolean jaTemPendente = !verificacaoLocadorRepository
                .findByUserIdAndStatus(id, VerificacaoLocador.Status.PENDENTE.name())
                .isEmpty();
        if (jaTemPendente) {
            throw new VerificacaoJaPendenteException();
        }

        String documentoUrl = documentStorageService.salvar(documento, "verificacoes");

        VerificacaoLocador verificacao = VerificacaoLocador.builder()
                .userId(usuario.getId())
                .status(VerificacaoLocador.Status.PENDENTE.name())
                .documentoUrl(documentoUrl)
                .build();
        verificacaoLocadorRepository.save(verificacao);

        return new VerificacaoLocadorResponseDTO(verificacao);
    }

    @Override
    public void promoverContaMista(UUID id, UUID requesterId) {
        User usuario = repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDonoOuAdmin(requesterId, id);

        if (usuario.getTipoConta() != User.Tipo.ESTUDANTE) {
            throw new TipoContaInvalidoException();
        }
        usuario.setTipoConta(User.Tipo.MISTO);
        repository.save(usuario);
    }

    @Override
    public FavoritoResponseDTO adicionarFavorito(UUID id, UUID adId, UUID requesterId) {
        repository.findById(id).orElseThrow(UserNotFoundException::new);
        // Só faz sentido favoritar em nome de si mesmo; ADMIN não precisa
        // gerenciar favoritos de terceiros, então exigimos o próprio dono.
        exigirDono(requesterId, id);

        if (!favoritoRepository.anuncioExiste(adId)) {
            throw new AnuncioNaoEncontradoException();
        }

        Favorito favorito = Favorito.builder()
                .userId(id)
                .adId(adId)
                .build();
        try {
            favoritoRepository.save(favorito);
        } catch (DataIntegrityViolationException e) {
            // Violação do índice único uq_favorites_user_ad.
            throw new FavoritoDuplicadoException();
        }
        return new FavoritoResponseDTO(favorito);
    }

    @Override
    public List<FavoritoResponseDTO> listarFavoritos(UUID id, UUID requesterId) {
        repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDonoOuAdmin(requesterId, id);

        return favoritoRepository.findByUserId(id).stream()
                .map(FavoritoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void removerFavorito(UUID id, UUID adId, UUID requesterId) {
        repository.findById(id).orElseThrow(UserNotFoundException::new);
        exigirDono(requesterId, id);

        // Idempotente de propósito: remover um favorito que já não existe
        // (ou nunca existiu) não é um erro do ponto de vista do cliente.
        favoritoRepository.deleteByUserIdAndAdId(id, adId);
    }

    @Override
    public void excluirUsuario(UUID id, UUID requesterId) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException();
        }
        exigirDonoOuAdmin(requesterId, id);

        // RNF/LEG-02: a exclusão em si não precisa "limpar manualmente" cada
        // tabela relacionada — os ON DELETE CASCADE/SET NULL definidos nas
        // migrations do T5.2 já fazem o trabalho certo:
        //   - reviews.avaliador_id/avaliado_id -> SET NULL (anonimiza, mas
        //     preserva a avaliação em si);
        //   - properties/ads/roommate_profiles/favorites/contacts/
        //     verificacao_locador -> CASCADE (dados pessoais realmente somem);
        //   - audit_logs.user_id -> SET NULL (mantém o log de auditoria).
        repository.deleteById(id);
    }

    // -------------------------------------------------------------------
    // Helpers de autorização
    //
    // Placeholder ATÉ o T5.3 (autenticação/JWT) existir: por enquanto quem
    // está "logado" é informado pelo cliente via header X-User-Id (ver
    // UserController/ContatoController). Não é seguro por si só (qualquer
    // um pode mandar o header que quiser), mas isola a REGRA de autorização
    // ("dono ou ADMIN") do transporte — quando o T5.3 existir, essa mesma
    // lógica passa a receber o id extraído do token JWT em vez do header.
    // -------------------------------------------------------------------

    private void exigirDonoOuAdmin(UUID requesterId, UUID targetId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        if (requesterId.equals(targetId)) {
            return;
        }
        User requester = repository.findById(requesterId).orElseThrow(AcessoNegadoException::new);
        if (requester.getTipoConta() != User.Tipo.ADMIN) {
            throw new AcessoNegadoException();
        }
    }

    private void exigirDono(UUID requesterId, UUID targetId) {
        if (requesterId == null || !requesterId.equals(targetId)) {
            throw new AcessoNegadoException();
        }
    }
}
