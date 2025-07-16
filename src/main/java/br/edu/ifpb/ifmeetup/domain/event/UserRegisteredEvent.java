package br.edu.ifpb.ifmeetup.domain.event;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio disparado quando um usuário é registrado com sucesso.
 * Este evento será tratado após o commit da transação para envio de emails.
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final User user;
    private final String verificationToken;
    
    public UserRegisteredEvent(Object source, User user, String verificationToken) {
        super(source);
        this.user = user;
        this.verificationToken = verificationToken;
    }
    
    /**
     * Cria um novo evento de registro de usuário.
     * 
     * @param source o objeto que disparou o evento (geralmente o service)
     * @param user o usuário que foi registrado
     * @param verificationToken o token de verificação gerado para o usuário
     * @return nova instância do evento
     */
    public static UserRegisteredEvent of(Object source, User user, String verificationToken) {
        return new UserRegisteredEvent(source, user, verificationToken);
    }
} 