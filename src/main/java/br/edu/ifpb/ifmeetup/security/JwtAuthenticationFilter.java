package br.edu.ifpb.ifmeetup.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String header = request.getHeader("Authorization");
            String token = null;

            // Extrair token do header Authorization
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
                log.debug("Token JWT extraído do header");
            }

            // Validar e processar token
            if (token != null && tokenProvider.validateToken(token)) {
                String email = tokenProvider.getEmailFromToken(token);
                log.debug("Token válido para email: {}", email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.debug("UserDetails carregado - Tipo: {}", userDetails.getClass().getSimpleName());


                User user = null;

                if (userDetails instanceof User) {
                    user = (User) userDetails;
                    log.debug("Cast para User realizado com sucesso - ID: {}", user.getId());
                } else {
                    log.error("ERRO: UserDetails não é uma instância de User. Tipo encontrado: {} para email: {}",
                            userDetails.getClass().getName(), email);
                    log.error("Verifique se o UserDetailsService retorna objetos User diretamente");
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                // Definir contexto de segurança
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Autenticação bem-sucedida para usuário: {} (ID: {})", email, user.getId());

            } else if (token != null) {
                log.debug("Token inválido ou expirado");
                SecurityContextHolder.clearContext();
            } else {
                log.debug("Nenhum token JWT encontrado no header Authorization");
            }

        } catch (Exception e) {
            log.error("Erro durante processamento JWT: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/auth/login") ||
                path.equals("/auth/register") ||
                path.equals("/auth/verify") ||
                path.equals("/auth/reset-password") ||
                path.equals("/auth/forgot-password") ||
                path.startsWith("/public/") ||
                path.equals("/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v3/api-docs");
    }
}