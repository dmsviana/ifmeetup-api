package br.edu.ifpb.ifmeetup.service.auth;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando usuário pelo email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com email: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado com email: " + email);
                });

        log.debug("Usuário encontrado: {} (ID: {})", user.getEmail(), user.getId());
        log.debug("Usuário habilitado: {}", user.isEnabled());
        log.debug("Quantidade de roles: {}", user.getRoles().size());

        return user;
    }
}