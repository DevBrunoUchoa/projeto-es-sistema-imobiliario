package com.campusliving.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.dto.admin.AdminModerarDenunciaRequestDTO.Acao;
import com.campusliving.model.denuncia.Denuncia;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.denuncia.DenunciaRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.repository.usuario.VerificacaoLocadorRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private VerificacaoLocadorRepository verificacaoLocadorRepository;

    private AdminService service;

    private AdminService service() {
        if (service == null) {
            service = new AdminService(userRepository, denunciaRepository, anuncioRepository, verificacaoLocadorRepository);
        }
        return service;
    }

    private Denuncia denunciaDeAnuncio(UUID id, UUID anuncioId) {
        return Denuncia.builder()
                .id(id)
                .denuncianteId(UUID.randomUUID())
                .tipoAlvo(Denuncia.TipoAlvo.ANUNCIO)
                .alvoId(anuncioId)
                .motivo(Denuncia.Motivo.FRAUDE)
                .status(Denuncia.Status.PENDENTE)
                .contadorDenuncias(1)
                .build();
    }

    @Test
    void banirAnuncio_inativaAnuncioEResolveDenuncia() {
        UUID denId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Denuncia denuncia = denunciaDeAnuncio(denId, anuncioId);
        Anuncio anuncio = Anuncio.builder().id(anuncioId).status(Anuncio.Status.ATIVO).build();

        when(denunciaRepository.findById(denId)).thenReturn(Optional.of(denuncia));
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        service().moderarDenuncia(denId, Acao.BANIR_ANUNCIO, adminId);

        assertThat(anuncio.getStatus()).isEqualTo(Anuncio.Status.INATIVO);
        assertThat(denuncia.getStatus()).isEqualTo(Denuncia.Status.RESOLVIDA);
        assertThat(denuncia.getResolvidoPor()).isEqualTo(adminId);
        verify(anuncioRepository).save(anuncio);
        verify(denunciaRepository).save(denuncia);
    }

    @Test
    void arquivar_rejeitaDenunciaSemTocarNoAnuncio() {
        UUID denId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Denuncia denuncia = denunciaDeAnuncio(denId, UUID.randomUUID());

        when(denunciaRepository.findById(denId)).thenReturn(Optional.of(denuncia));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        service().moderarDenuncia(denId, Acao.ARQUIVAR, adminId);

        assertThat(denuncia.getStatus()).isEqualTo(Denuncia.Status.REJEITADA);
        assertThat(denuncia.getResolvidoPor()).isEqualTo(adminId);
        verify(anuncioRepository, never()).save(any());
    }
}
