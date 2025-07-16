package br.edu.ifpb.ifmeetup.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.ifmeetup.domain.entity.PasswordResetToken;
import br.edu.ifpb.ifmeetup.domain.entity.Role;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.entity.UserProfile;
import br.edu.ifpb.ifmeetup.domain.entity.VerificationToken;
import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import br.edu.ifpb.ifmeetup.domain.repository.auth.PasswordResetTokenRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.RoleRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.TokenBlacklistRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserProfileRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.VerificationTokenRepository;
import br.edu.ifpb.ifmeetup.dto.auth.request.ForgotPasswordRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.LoginRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.PasswordResetRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.RegisterRequest;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;
import br.edu.ifpb.ifmeetup.dto.user.response.UserResponse;
import br.edu.ifpb.ifmeetup.exception.AuthenticationException;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.EmailNotVerifiedException;
import br.edu.ifpb.ifmeetup.exception.UserAlreadyExistsException;
import br.edu.ifpb.ifmeetup.exception.ValidationException;
import br.edu.ifpb.ifmeetup.security.JwtTokenProvider;
import br.edu.ifpb.ifmeetup.domain.event.UserRegisteredEvent;
import br.edu.ifpb.ifmeetup.service.notification.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        log.debug("Login attempt for email: {}", request.email());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        
        if (!authentication.isAuthenticated()) {
            log.warn("Failed authentication attempt for email: {}", request.email());
            throw AuthenticationException.invalidCredentials();
        }
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", request.email());
                    return AuthenticationException.invalidCredentials();
                });
        
        if (!user.isEmailVerified()) {
            log.info("Login attempt with unverified email: {}", request.email());
            throw AuthenticationException.emailNotVerified(user.getEmail());
        }
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        String token = tokenProvider.createToken(user);
        UUID sessionId = tokenProvider.getSessionIdFromToken(token);
        LocalDateTime expiresAt = tokenProvider.getExpirationDateFromToken(token);
        
        log.info("Successful login for user: {} - Session ID: {}", user.getEmail(), sessionId);
        
        return AuthResponse.success(
                "Login realizado com sucesso",
                UserResponse.fromEntity(user),
                sessionId,
                expiresAt,
                token
        );
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registration attempt for email: {} with profile: {}", request.email(), request.profileType());
        
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration attempt for already existing email: {}", request.email());
            throw new UserAlreadyExistsException("Email já cadastrado");
        }
        
        ProfileType profileType = request.profileType();
        if (profileType == ProfileType.ADMIN || profileType == ProfileType.COORDINATOR) {
            throw ValidationException.forField("profileType", profileType, 
                "Tipo de perfil não permitido para registro público");
        }
        
        Role defaultRole = getDefaultRoleForProfileType(request);
        
        User user = new User();
        
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName()); 
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        user.setRoles(Set.of(defaultRole));
        
        User savedUser = userRepository.save(user);
        
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setProfileType(request.profileType());
        savedUser.setProfile(profile);
        userProfileRepository.save(profile);
        
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(verificationToken);

        // Publicar evento para envio de emails após commit da transação
        // Isso garante que emails só sejam enviados se o registro for bem-sucedido
        UserRegisteredEvent event = UserRegisteredEvent.of(this, savedUser, verificationToken.getToken());
        eventPublisher.publishEvent(event);

        log.info("User registered successfully: {} - Event published for email processing", savedUser.getEmail());
        
        return AuthResponse.success(
            "Usuário registrado com sucesso. Verifique seu email para ativar sua conta.",
            UserResponse.fromEntity(savedUser),
            null,
            null,
            null
        );
    }
    
    protected Role getDefaultRoleForProfileType(RegisterRequest request) {
        
       
        String roleName;
        switch (request.profileType()) {
            case ADMIN -> roleName = "ADMIN";
            case COORDINATOR -> roleName = "COORDINATOR";
            case TEACHER -> roleName = "TEACHER";
            case STUDENT -> roleName = "STUDENT";
            default -> roleName = "STUDENT";
        }
        
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Perfil não encontrado: " + roleName)
                    .addDetail("roleName", roleName));
    }
    
    @Transactional
    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
        
        try {

            String token = tokenProvider.extractTokenFromHeader(request);
            
            if (token != null) {

                tokenProvider.invalidateToken(token);
                log.debug("Token invalidado durante logout");

            }
            
            SecurityContextHolder.clearContext();
            
            tokenProvider.clearTokenCookie(response);
            
            return AuthResponse.success("Logout realizado com sucesso");
        
        } catch (Exception e) {

            log.error("Erro durante logout: {}", e.getMessage());
            SecurityContextHolder.clearContext();

            return AuthResponse.success("Logout realizado com sucesso");
        }
    }
    
    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ValidationException.forField("email", request.email(), "Usuário não encontrado"));
        
        passwordResetTokenRepository.deleteByUserId(user.getId());
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);
        
        emailService.sendPasswordResetEmail(
            user.getEmail(),
            user.getFirstName(),
            resetToken.getToken()
        );
        
        return AuthResponse.success("Instruções para redefinição de senha foram enviadas para seu email.");
    }
    
    @Transactional
    public AuthResponse resetPassword(PasswordResetRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> AuthenticationException.invalidToken("password-reset"));
        
        if (resetToken.isUsed()) {
            throw AuthenticationException.tokenAlreadyUsed("password-reset");
        }
        
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw AuthenticationException.expiredToken("password-reset");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        return AuthResponse.success("Senha redefinida com sucesso");
    }
    
    @Transactional(readOnly = true)
    public AuthResponse verifyToken(String token) {
        return getVerificationToken(token)
                .map(vt -> AuthResponse.success("Token válido"))
                .orElseThrow(() -> AuthenticationException.invalidToken("verification"));
    }
    
    @Transactional
    public AuthResponse verifyAccount(String token) {
        // Remover espaços em branco no início e no fim do token
        String trimmedToken = token.trim();
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(trimmedToken)
                .orElseThrow(() -> AuthenticationException.invalidToken("verification"));
        
        if (verificationToken.isUsed()) {
            throw AuthenticationException.tokenAlreadyUsed("verification");
        }
        
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw AuthenticationException.expiredToken("verification");
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
        
        return AuthResponse.success(
            "Conta verificada com sucesso",
            UserResponse.fromEntity(user),
            null,
            null,
            null
        );
    }
    
    private Optional<VerificationToken> getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .filter(vt -> !vt.isUsed())
                .filter(vt -> vt.getExpiryDate().isAfter(LocalDateTime.now()));
    }
    
    /**
     * Obtém o usuário autenticado a partir do SecurityContextHolder
     * @return O usuário autenticado
     * @throws UsernameNotFoundException Se não houver usuário autenticado
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("Usuário não autenticado");
        }
        
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }
    
    /**
     * Retorna os dados do usuário atualmente autenticado
     * @return Resposta com dados do usuário
     */
    @Transactional(readOnly = true)
    public AuthResponse me() {
        User user = getCurrentUser();
        return AuthResponse.success(
            "Dados do usuário recuperados com sucesso",
            UserResponse.fromEntity(user),
            null,
            null,
            null
        );
    }
} 