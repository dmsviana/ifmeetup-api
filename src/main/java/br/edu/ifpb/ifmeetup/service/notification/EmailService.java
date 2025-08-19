package br.edu.ifpb.ifmeetup.service.notification;

import br.edu.ifpb.ifmeetup.config.ApplicationConfig;
import br.edu.ifpb.ifmeetup.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final ApplicationConfig applicationConfig;

    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado com sucesso para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email para: {}", to, e);
            throw new EmailSendException("Erro ao enviar email para: " + to, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            Map<String, Object> variables = Map.of(
                "name", name,
                "applicationUrl", applicationConfig.getApplicationUrl()
            );

            sendTemplateEmail(
                to,
                "Bem-vindo ao IFMeetup!",
                "welcome",
                variables
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email de boas-vindas para: {}", to, e);
            throw new EmailSendException("Erro ao enviar email de boas-vindas", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            
            Map<String, Object> variables = Map.of(
                "name", name,
                "resetLink", applicationConfig.getApplicationUrl() + "/auth/reset-password?token=" + encodedToken,
                "token", token
            );

            sendTemplateEmail(
                to,
                "Recuperação de Senha - IFMeetup",
                "password-reset",
                variables
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email de recuperação de senha para: {}", to, e);
            throw new EmailSendException("Erro ao enviar email de recuperação de senha", e);
        }
    }

    @Async
    public void sendEmailVerification(String to, String name, String token) {
        try {

            Map<String, Object> variables = Map.of(
                    "name", name,
                    "token", token
            );

            sendTemplateEmail(
                    to,
                    "Verificação de Email - IFMeetup",
                    "email-verification",
                    variables
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email de verificação para: {}", to, e);
            throw new EmailSendException("Erro ao enviar email de verificação", e);
        }
    }
} 