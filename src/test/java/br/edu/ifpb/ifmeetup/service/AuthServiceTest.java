package br.edu.ifpb.ifmeetup.service;

import br.edu.ifpb.ifmeetup.domain.entity.*;
import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import br.edu.ifpb.ifmeetup.domain.repository.auth.PasswordResetTokenRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.RoleRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserProfileRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.VerificationTokenRepository;
import br.edu.ifpb.ifmeetup.dto.auth.request.ForgotPasswordRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.LoginRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.PasswordResetRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.RegisterRequest;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.EmailNotVerifiedException;
import br.edu.ifpb.ifmeetup.exception.UserAlreadyExistsException;
import br.edu.ifpb.ifmeetup.security.JwtTokenProvider;
import br.edu.ifpb.ifmeetup.service.auth.AuthService;
import br.edu.ifpb.ifmeetup.service.notification.EmailService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private Authentication authentication;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private VerificationToken testVerificationToken;
    private PasswordResetToken testResetToken;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("Maria");
        testUser.setLastName("Silva");
        testUser.setEmail("maria.silva@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Configurar role de teste
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("STUDENT");
        testUser.setRoles(Set.of(testRole));

        // Configurar token de verificação
        testVerificationToken = new VerificationToken();
        testVerificationToken.setId(UUID.randomUUID());
        testVerificationToken.setToken("verification-token");
        testVerificationToken.setUser(testUser);
        testVerificationToken.setCreatedAt(LocalDateTime.now());
        testVerificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        testVerificationToken.setUsed(false);

        // Configurar token de redefinição de senha
        testResetToken = new PasswordResetToken();
        testResetToken.setId(UUID.randomUUID());
        testResetToken.setToken("reset-token");
        testResetToken.setUser(testUser);
        testResetToken.setCreatedAt(LocalDateTime.now());
        testResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        testResetToken.setUsed(false);
    }

    @Nested
    @DisplayName("Testes de login")
    class LoginTests {

        private HttpServletResponse response;
        private LoginRequest request;
        private User user;

        @BeforeEach
        void setUp() {
            response = mock(HttpServletResponse.class);
            request = new LoginRequest("test@example.com", "password");
            
            user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setPassword("encodedPassword");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setEmailVerified(true);
        }

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void shouldLoginSuccessfully() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(tokenProvider.createToken(user)).thenReturn("dummyToken");
            when(tokenProvider.getSessionIdFromToken("dummyToken")).thenReturn(UUID.randomUUID());
            when(tokenProvider.getExpirationDateFromToken("dummyToken")).thenReturn(LocalDateTime.now().plusHours(24));
            
            // Act
            AuthResponse result = authService.login(request, response);
            
            // Assert
            assertNotNull(result);
            assertEquals("Login realizado com sucesso", result.message());
            assertNotNull(result.token());
            assertEquals("dummyToken", result.token());
            assertNotNull(result.user());
            assertNotNull(result.sessionId());
            assertNotNull(result.expiresAt());
            
            // Não verificamos mais setTokenCookie pois usamos apenas header Authorization
            verify(userRepository).save(user);
            verify(tokenProvider).createToken(user);
        }

        @Test
        @DisplayName("Deve lançar exceção quando credenciais são inválidas")
        void shouldThrowExceptionWhenCredentialsAreInvalid() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);
            
            // Act & Assert
            assertThrows(BusinessValidationException.class, () -> {
                authService.login(request, response);
            });
        }

        @Test
        @DisplayName("Deve lançar exceção quando email não está verificado")
        void shouldThrowExceptionWhenEmailNotVerified() {
            // Arrange
            user.setEmailVerified(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            
            // Act & Assert
            assertThrows(EmailNotVerifiedException.class, () -> {
                authService.login(request, response);
            });
        }
    }

    @Nested
    @DisplayName("Testes de registro")
    class RegisterTests {

        @ParameterizedTest
        @EnumSource(value = ProfileType.class, names = {"STUDENT", "TEACHER"})
        @DisplayName("Deve registrar usuário com sucesso para perfis permitidos (STUDENT e TEACHER)")
        void shouldRegisterUserSuccessfully(ProfileType profileType) {
            // Arrange
            RegisterRequest registerRequest = new RegisterRequest(
                    "Maria",
                    "Silva",
                    "novo.usuario@example.com",
                    "(83) 99999-9999",
                    "senha123",
                    profileType
            );

            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(roleRepository.findByName(anyString())).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(emailService).sendEmailVerification(anyString(), anyString(), anyString());
            doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());
            
            // Act
            AuthResponse response = authService.register(registerRequest);
            
            // Assert
            assertNotNull(response);
            assertEquals("Usuário registrado com sucesso. Verifique seu email para ativar sua conta.", response.message());
            assertNotNull(response.user());
            
            verify(userRepository).save(any(User.class));
            verify(userProfileRepository).save(any(UserProfile.class));
            verify(verificationTokenRepository).save(any(VerificationToken.class));
            verify(emailService).sendEmailVerification(anyString(), anyString(), anyString());
            verify(emailService).sendWelcomeEmail(anyString(), anyString());
            
            // Verificar captura de argumentos
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            
            assertEquals(registerRequest.firstName(), savedUser.getFirstName());
            assertEquals(registerRequest.lastName(), savedUser.getLastName());
            assertEquals(registerRequest.email(), savedUser.getEmail());
            assertEquals(registerRequest.phoneNumber(), savedUser.getPhoneNumber());
            assertEquals("hashedPassword", savedUser.getPassword());
            assertFalse(savedUser.isEmailVerified());
            
            ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
            verify(userProfileRepository).save(profileCaptor.capture());
            UserProfile savedProfile = profileCaptor.getValue();
            
            assertEquals(profileType, savedProfile.getProfileType());
        }

        @ParameterizedTest
        @EnumSource(value = ProfileType.class, names = {"ADMIN", "COORDINATOR"})
        @DisplayName("Deve lançar exceção ao tentar registrar com perfis privilegiados")
        void shouldThrowExceptionWhenRegisteringWithPrivilegedProfiles(ProfileType profileType) {
            // Arrange
            RegisterRequest registerRequest = new RegisterRequest(
                    "Admin",
                    "User",
                    "admin@example.com",
                    "(83) 99999-9999",
                    "senha123",
                    profileType
            );
            
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.register(registerRequest));
            
            assertEquals("Tipo de perfil não permitido para registro público", exception.getMessage());
            
            // Verificar que nenhum usuário foi salvo
            verify(userRepository, never()).save(any(User.class));
            verify(userProfileRepository, never()).save(any(UserProfile.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existir")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            RegisterRequest registerRequest = new RegisterRequest(
                    "Maria",
                    "Silva",
                    "existente@example.com",
                    "(83) 99999-9999",
                    "senha123",
                    ProfileType.STUDENT
            );

            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);
            
            // Act & Assert
            UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
                    () -> authService.register(registerRequest));
            
            assertEquals("Email já cadastrado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(userProfileRepository, never()).save(any(UserProfile.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando perfil não for encontrado")
        void shouldThrowExceptionWhenRoleNotFound() {
            // Arrange
            RegisterRequest registerRequest = new RegisterRequest(
                    "Maria",
                    "Silva",
                    "novo.usuario@example.com",
                    "(83) 99999-9999",
                    "senha123",
                    ProfileType.STUDENT
            );

            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.register(registerRequest));
            
            assertTrue(exception.getMessage().contains("Perfil não encontrado"));
            verify(userRepository, never()).save(any(User.class));
            verify(userProfileRepository, never()).save(any(UserProfile.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        }
    }

    @Nested
    @DisplayName("Testes de esquecimento de senha")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Deve processar solicitação de esquecimento de senha com sucesso")
        void shouldProcessForgotPasswordRequestSuccessfully() {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest("maria.silva@example.com");
            
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
            
            // Act
            AuthResponse response = authService.forgotPassword(request);
            
            // Assert
            assertNotNull(response);
            assertEquals("Instruções para redefinição de senha foram enviadas para seu email.", response.message());
            assertNull(response.user());
            
            verify(passwordResetTokenRepository).deleteByUserId(testUser.getId());
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq(testUser.getEmail()), eq(testUser.getFirstName()), anyString());
            
            // Verificar token gerado
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(passwordResetTokenRepository).save(tokenCaptor.capture());
            PasswordResetToken savedToken = tokenCaptor.getValue();
            
            assertNotNull(savedToken.getToken());
            assertEquals(testUser, savedToken.getUser());
            assertNotNull(savedToken.getExpiryDate());
            assertFalse(savedToken.isUsed());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for encontrado")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest("nao.existe@example.com");
            
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.forgotPassword(request));
            
            assertEquals("Usuário não encontrado", exception.getMessage());
            verify(passwordResetTokenRepository, never()).deleteByUserId(any());
            verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        }
    }

    @Nested
    @DisplayName("Testes de redefinição de senha")
    class ResetPasswordTests {

        @Test
        @DisplayName("Deve redefinir senha com sucesso")
        void shouldResetPasswordSuccessfully() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("reset-token", "nova_senha123");
            
            when(passwordResetTokenRepository.findByToken(request.token())).thenReturn(Optional.of(testResetToken));
            when(passwordEncoder.encode(request.newPassword())).thenReturn("hashed_new_password");
            
            // Act
            AuthResponse response = authService.resetPassword(request);
            
            // Assert
            assertNotNull(response);
            assertEquals("Senha redefinida com sucesso", response.message());
            assertNull(response.user());
            
            verify(userRepository).save(testUser);
            verify(passwordResetTokenRepository).save(testResetToken);
            
            assertEquals("hashed_new_password", testUser.getPassword());
            assertTrue(testResetToken.isUsed());
        }

        @Test
        @DisplayName("Deve lançar exceção quando token não for encontrado")
        void shouldThrowExceptionWhenTokenNotFound() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("token-inexistente", "nova_senha123");
            
            when(passwordResetTokenRepository.findByToken(request.token())).thenReturn(Optional.empty());
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.resetPassword(request));
            
            assertEquals("Token inválido", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando token já estiver usado")
        void shouldThrowExceptionWhenTokenAlreadyUsed() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("reset-token", "nova_senha123");
            testResetToken.setUsed(true);
            
            when(passwordResetTokenRepository.findByToken(request.token())).thenReturn(Optional.of(testResetToken));
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.resetPassword(request));
            
            assertEquals("Token já utilizado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
            
            // Restaurar estado
            testResetToken.setUsed(false);
        }

        @Test
        @DisplayName("Deve lançar exceção quando token estiver expirado")
        void shouldThrowExceptionWhenTokenExpired() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("reset-token", "nova_senha123");
            testResetToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));
            
            when(passwordResetTokenRepository.findByToken(request.token())).thenReturn(Optional.of(testResetToken));
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.resetPassword(request));
            
            assertEquals("Token expirado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
            
            // Restaurar estado
            testResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        }
    }

    @Nested
    @DisplayName("Testes de verificação de token")
    class VerifyTokenTests {

        @Test
        @DisplayName("Deve verificar token com sucesso")
        void shouldVerifyTokenSuccessfully() {
            // Arrange
            String token = "verification-token";
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));
            
            // Act
            AuthResponse response = authService.verifyToken(token);
            
            // Assert
            assertNotNull(response);
            assertEquals("Token válido", response.message());
            assertNull(response.user());
        }

        @Test
        @DisplayName("Deve lançar exceção quando token de verificação não for encontrado")
        void shouldThrowExceptionWhenVerificationTokenNotFound() {
            // Arrange
            String token = "token-inexistente";
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.empty());
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyToken(token));
            
            assertEquals("Token inválido ou expirado", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando token de verificação estiver usado")
        void shouldThrowExceptionWhenVerificationTokenUsed() {
            // Arrange
            String token = "verification-token";
            testVerificationToken.setUsed(true);
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyToken(token));
            
            assertEquals("Token inválido ou expirado", exception.getMessage());
            
            // Restaurar estado
            testVerificationToken.setUsed(false);
        }

        @Test
        @DisplayName("Deve lançar exceção quando token de verificação estiver expirado")
        void shouldThrowExceptionWhenVerificationTokenExpired() {
            // Arrange
            String token = "verification-token";
            testVerificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyToken(token));
            
            assertEquals("Token inválido ou expirado", exception.getMessage());
            
            // Restaurar estado
            testVerificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        }
    }

    @Nested
    @DisplayName("Testes de verificação de conta")
    class VerifyAccountTests {

        @Test
        @DisplayName("Deve verificar conta com sucesso")
        void shouldVerifyAccountSuccessfully() {
            // Arrange
            String token = "verification-token";
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));
            
            // Act
            AuthResponse response = authService.verifyAccount(token);
            
            // Assert
            assertNotNull(response);
            assertEquals("Conta verificada com sucesso", response.message());
            assertNotNull(response.user());
            assertEquals(testUser.getId(), response.user().id());
            
            verify(userRepository).save(testUser);
            verify(verificationTokenRepository).save(testVerificationToken);
            
            assertTrue(testUser.isEmailVerified());
            assertTrue(testVerificationToken.isUsed());
        }

        @Test
        @DisplayName("Deve lançar exceção quando token não for encontrado para verificação de conta")
        void shouldThrowExceptionWhenTokenNotFoundForAccountVerification() {
            // Arrange
            String token = "token-inexistente";
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.empty());
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyAccount(token));
            
            assertEquals("Token inválido ou expirado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando token já estiver usado para verificação de conta")
        void shouldThrowExceptionWhenTokenAlreadyUsedForAccountVerification() {
            // Arrange
            String token = "verification-token";
            testVerificationToken.setUsed(true);
            
            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));
            
            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyAccount(token));
            
            assertEquals("Token já utilizado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
            
            // Restaurar estado
            testVerificationToken.setUsed(false);
        }

        @Test
        @DisplayName("Deve lançar exceção quando token de verificação estiver expirado para verificação de conta")
        void shouldThrowExceptionWhenTokenExpiredForAccountVerification() {

            // Arrange
            String token = "verification-token";
            testVerificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));

            when(verificationTokenRepository.findByToken(token))
                    .thenReturn(Optional.of(testVerificationToken));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class, 
                    () -> authService.verifyAccount(token));

            assertEquals("Token inválido ou expirado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
            
        }
    }

    @Nested
    @DisplayName("Testes de obtenção de role por tipo de perfil")
    class GetDefaultRoleForProfileTypeTests {

        private static Stream<Arguments> provideProfileTypesAndRoleNames() {
            return Stream.of(
                    Arguments.of(ProfileType.STUDENT, "STUDENT"),
                    Arguments.of(ProfileType.TEACHER, "TEACHER")
            );
        }

        @ParameterizedTest
        @MethodSource("provideProfileTypesAndRoleNames")
        @DisplayName("Deve obter role correta para cada tipo de perfil permitido")
        void shouldGetCorrectRoleForProfileType(ProfileType profileType, String expectedRoleName) {
            // Arrange
            RegisterRequest registerRequest = new RegisterRequest(
                    "Maria",
                    "Silva",
                    "maria.silva@example.com",
                    "(83) 99999-9999",
                    "senha123",
                    profileType
            );
            
            Role role = new Role();
            role.setId(UUID.randomUUID());
            role.setName(expectedRoleName);
            
            when(roleRepository.findByName(expectedRoleName)).thenReturn(Optional.of(role));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            
            // Act
            authService.register(registerRequest);
            
            // Assert
            verify(roleRepository).findByName(expectedRoleName);
            
            // Capturar o usuário salvo para verificar a role
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            
            assertTrue(savedUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals(expectedRoleName)));
        }
        
        // @Test
        // @DisplayName("Deve obter role de ADMIN corretamente quando chamado diretamente")
        // void shouldGetAdminRoleCorrectlyWhenCalledDirectly() {
        //     // Arrange
        //     RegisterRequest registerRequest = new RegisterRequest(
        //             "Admin",
        //             "User",
        //             "admin@example.com",
        //             "(83) 99999-9999",
        //             "senha123",
        //             ProfileType.ADMIN
        //     );
            
        //     Role adminRole = new Role();
        //     adminRole.setId(UUID.randomUUID());
        //     adminRole.setName("ADMIN");
            
        //     when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            
        //     // Act
        //     Role resultRole = authService.getDefaultRoleForProfileType(registerRequest);
            
        //     // Assert
        //     assertEquals("ADMIN", resultRole.getName());
        //     verify(roleRepository).findByName("ADMIN");
        // }
    }

    @Nested
    @DisplayName("Testes do endpoint /me")
    class MeEndpointTests {

        @Mock
        private SecurityContext securityContext;
        
        @BeforeEach
        void setUp() {
            // Configurar SecurityContextHolder mock
            SecurityContextHolder.clearContext();
        }
        
        @Test
        @DisplayName("Deve retornar dados do usuário autenticado com sucesso")
        void shouldReturnAuthenticatedUserSuccessfully() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("maria.silva@example.com");
            when(userRepository.findByEmail("maria.silva@example.com")).thenReturn(Optional.of(testUser));
            
            SecurityContextHolder.setContext(securityContext);
            
            // Act
            AuthResponse response = authService.me();
            
            // Assert
            assertNotNull(response);
            assertEquals("Dados do usuário recuperados com sucesso", response.message());
            assertNotNull(response.user());
            assertEquals(testUser.getId(), response.user().id());
            assertEquals("Maria Silva", response.user().name());
            assertEquals("maria.silva@example.com", response.user().email());
            assertNotNull(response.user().roles());
            
            verify(userRepository).findByEmail("maria.silva@example.com");
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando usuário não estiver autenticado")
        void shouldThrowExceptionWhenUserNotAuthenticated() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);
            SecurityContextHolder.setContext(securityContext);
            
            // Act & Assert
            assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentUser());
            
            verify(userRepository, never()).findByEmail(anyString());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando SecurityContext estiver vazio")
        void shouldThrowExceptionWhenSecurityContextIsEmpty() {
            // Arrange
            SecurityContextHolder.clearContext();
            
            // Act & Assert
            assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentUser());
            
            verify(userRepository, never()).findByEmail(anyString());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando usuário for 'anonymousUser'")
        void shouldThrowExceptionWhenUserIsAnonymous() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            SecurityContextHolder.setContext(securityContext);
            
            // Act & Assert
            assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentUser());
            
            verify(userRepository, never()).findByEmail(anyString());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando usuário não for encontrado no banco de dados")
        void shouldThrowExceptionWhenUserNotFoundInDatabase() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("usuario.nao.existente@example.com");
            when(userRepository.findByEmail("usuario.nao.existente@example.com")).thenReturn(Optional.empty());
            SecurityContextHolder.setContext(securityContext);
            
            // Act & Assert
            assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentUser());
            
            verify(userRepository).findByEmail("usuario.nao.existente@example.com");
        }
    }
} 