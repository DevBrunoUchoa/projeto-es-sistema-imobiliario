package com.campusliving.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mails transacionais (verificação de cadastro, reset de senha e
 * notificação de interesse — RF-05, RF-04, RF-38).
 *
 * <p>O envio é assíncrono para não bloquear a requisição. Quando o SMTP não
 * está configurado (sem bean {@link JavaMailSender} ou {@code app.mail.enabled=false}),
 * o conteúdo — incluindo o link — é apenas registrado em log. Assim o fluxo
 * continua testável localmente e, principalmente, o token nunca é devolvido no
 * corpo da resposta HTTP.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:no-reply@campusliving.app}")
    private String remetente;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /** RF-05: link de verificação de e-mail (expira em 24h). */
    @Async
    public void enviarVerificacaoEmail(String destino, String nome, String token) {
        String link = frontendUrl + "/auth/verificar-email/" + token;
        enviar(destino, "Confirme seu e-mail — Campus Living",
                "Olá, " + nome + "!\n\n"
                        + "Confirme seu cadastro acessando o link abaixo:\n" + link + "\n\n"
                        + "O link expira em 24 horas.");
    }

    /** RF-04: link de redefinição de senha (expira em 1h). */
    @Async
    public void enviarResetSenha(String destino, String nome, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        enviar(destino, "Redefinição de senha — Campus Living",
                "Olá, " + nome + "!\n\n"
                        + "Recebemos um pedido para redefinir sua senha. Use o link abaixo:\n" + link + "\n\n"
                        + "O link expira em 1 hora. Se não foi você, ignore este e-mail.");
    }

    /** RF-38: avisa o locador quando um estudante registra interesse. */
    @Async
    public void enviarNotificacaoInteresse(String locadorEmail, String locadorNome,
            String estudanteNome, String anuncioTitulo, String mensagemPreview) {
        enviar(locadorEmail, "Novo interesse no seu anúncio — Campus Living",
                "Olá, " + locadorNome + "!\n\n"
                        + estudanteNome + " demonstrou interesse no anúncio \"" + anuncioTitulo + "\".\n\n"
                        + "Mensagem: " + mensagemPreview + "\n\n"
                        + "Acesse a plataforma para responder.");
    }

    private void enviar(String destino, String assunto, String corpo) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!enabled || sender == null) {
            log.info("[e-mail desabilitado] destino={} assunto=\"{}\"\n{}", destino, assunto, corpo);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(destino);
            msg.setSubject(assunto);
            msg.setText(corpo);
            sender.send(msg);
            log.info("E-mail enviado para {} (assunto: {})", destino, assunto);
        } catch (Exception e) {
            // RF-38 (fluxo secundário): falha de entrega é registrada em log,
            // sem quebrar a operação principal que disparou o e-mail.
            log.error("Falha ao enviar e-mail para {}: {}", destino, e.getMessage());
        }
    }
}
