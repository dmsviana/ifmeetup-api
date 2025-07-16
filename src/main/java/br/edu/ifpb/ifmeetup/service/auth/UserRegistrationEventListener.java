package br.edu.ifpb.ifmeetup.service.auth;

import br.edu.ifpb.ifmeetup.domain.event.UserRegisteredEvent;
import br.edu.ifpb.ifmeetup.exception.ExternalServiceException;
import br.edu.ifpb.ifmeetup.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener para eventos de registro de usuário.
 * Processa envio de emails após o commit bem-sucedido da transação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventListener {
    
    private final EmailService emailService;
    
    /**
     * Processa o evento de usuário registrado após o commit da transação.
     * Este método é executado de forma assíncrona para não impactar o tempo de resposta.
     * 
     * @param event o evento de registro de usuário
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Processing user registration event for user: {}", event.getUser().getEmail());
        
        try {
            // Enviar email de verificação
            sendVerificationEmail(event);
            
            // Enviar email de boas-vindas
            sendWelcomeEmail(event);
            
            log.info("Successfully sent registration emails for user: {}", event.getUser().getEmail());
            
        } catch (Exception ex) {
            // Registrar a falha, mas não deixar que ela afete o processo de registro
            log.error("Failed to send registration emails for user: {} - Error: {}", 
                    event.getUser().getEmail(), ex.getMessage(), ex);
            
            // Opcionalmente, pode-se implementar uma estratégia de retry aqui
            // ou armazenar o evento em uma fila para reprocessamento posterior
        }
    }
    
    /**
     * Envia o email de verificação de forma segura.
     */
    private void sendVerificationEmail(UserRegisteredEvent event) {
        try {
            emailService.sendEmailVerification(
                    event.getUser().getEmail(),
                    event.getUser().getFirstName(),
                    event.getVerificationToken()
            );
            
            log.debug("Verification email sent successfully for user: {}", event.getUser().getEmail());
            
        } catch (Exception ex) {
            log.error("Failed to send verification email for user: {} - Error: {}", 
                    event.getUser().getEmail(), ex.getMessage());
            throw ExternalServiceException.emailFailure(
                    event.getUser().getEmail(), 
                    "verification", 
                    ex
            );
        }
    }
    
    /**
     * Envia o email de boas-vindas de forma segura.
     */
    private void sendWelcomeEmail(UserRegisteredEvent event) {
        try {
            emailService.sendWelcomeEmail(
                    event.getUser().getEmail(),
                    event.getUser().getFirstName()
            );
            
            log.debug("Welcome email sent successfully for user: {}", event.getUser().getEmail());
            
        } catch (Exception ex) {
            log.error("Failed to send welcome email for user: {} - Error: {}", 
                    event.getUser().getEmail(), ex.getMessage());
            throw ExternalServiceException.emailFailure(
                    event.getUser().getEmail(), 
                    "welcome", 
                    ex
            );
        }
    }
} 