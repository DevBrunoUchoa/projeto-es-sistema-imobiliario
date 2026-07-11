package com.campusliving.service.avaliacao;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Filtro de palavrão para o comentário da avaliação (RF-29, Fluxo Secundário
 * 1: "filtro automático bloqueia e adverte usuário").
 *
 * <p><b>Escopo deliberadamente mínimo</b>: isto é um filtro por lista de
 * palavras com normalização básica (minúsculas, remoção de acento,
 * whole-word match), suficiente para o RF descrito. NÃO é um sistema robusto
 * de moderação — não pega leetspeak elaborado, ofensas em outros idiomas,
 * nem contexto (sarcasmo, negação, etc.). Se o time quiser algo mais forte
 * depois, a troca é só a implementação desta classe: a interface pública
 * ({@link #contemPalavraImpropria(String)}) não muda.</p>
 */
@Component
public class PalavraoFilter {

    // Lista pequena e deliberadamente genérica — ajustem/completem conforme
    // o time achar necessário. Todas em minúsculas e sem acento, porque
    // normalizamos o texto de entrada antes de comparar.
    private static final Set<String> TERMOS_BLOQUEADOS = Set.of(
            "arrombado", "babaca", "bosta", "buceta", "caralho", "corno",
            "cu", "desgraca", "filho da puta", "fdp", "idiota", "imbecil",
            "merda", "otario", "piranha", "porra", "puta", "retardado",
            "vagabundo", "vagabunda", "viado"
    );

    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9 ]");

    public boolean contemPalavraImpropria(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        String normalizado = normalizar(texto);

        for (String termo : TERMOS_BLOQUEADOS) {
            String termoNormalizado = normalizar(termo);
            // \b garante palavra inteira: "classe" não deveria acender por
            // conter uma substring proibida sem ser essa a intenção.
            Pattern padrao = Pattern.compile("\\b" + Pattern.quote(termoNormalizado) + "\\b");
            if (padrao.matcher(normalizado).find()) {
                return true;
            }
        }
        return false;
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return NAO_ALFANUMERICO.matcher(semAcento).replaceAll(" ");
    }
}