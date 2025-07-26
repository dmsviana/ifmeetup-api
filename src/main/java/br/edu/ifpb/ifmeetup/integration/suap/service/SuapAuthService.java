package br.edu.ifpb.ifmeetup.integration.suap.service;

import br.edu.ifpb.ifmeetup.domain.entity.Role;
import br.edu.ifpb.ifmeetup.domain.entity.SuapUser;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.entity.UserProfile;
import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import br.edu.ifpb.ifmeetup.domain.repository.auth.RoleRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.SuapUserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserProfileRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;
import br.edu.ifpb.ifmeetup.dto.user.response.UserResponse;
import br.edu.ifpb.ifmeetup.exception.AuthenticationException;
import br.edu.ifpb.ifmeetup.exception.ExternalServiceException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.integration.suap.client.SuapAuthClient;
import br.edu.ifpb.ifmeetup.integration.suap.client.SuapDataClient;
import br.edu.ifpb.ifmeetup.integration.suap.dto.*;
import br.edu.ifpb.ifmeetup.integration.suap.mapper.SuapRoleMapper;
import br.edu.ifpb.ifmeetup.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço responsável pela autenticação e sincronização de usuários SUAP.
 * 
 * Implementa o fluxo completo de:
 * - Autenticação no SUAP
 * - Busca de dados do usuário
 * - Criação/atualização de usuários no sistema
 * - Sincronização de dados SUAP
 * - Geração de tokens JWT locais
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuapAuthService {

    private final SuapAuthClient suapAuthClient;
    private final SuapDataClient suapDataClient;
    private final SuapUserRepository suapUserRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final SuapRoleMapper suapRoleMapper;
    private final SuapSecurityValidator securityValidator;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Realiza login completo via SUAP com orquestração de todo o fluxo.
     * 
     * @param request  Credenciais SUAP (matrícula e senha)
     * @param response Response HTTP para configuração de cookies
     * @return Resposta de autenticação com token JWT do IFMeetup
     */
    @Transactional
    public AuthResponse loginWithSuap(SuapLoginRequest request, HttpServletResponse response) {
        log.info("Iniciando login SUAP para matrícula: {}", request.username());

        try {
            // 0. Validar credenciais de entrada
            securityValidator.validateCredentials(request);

            // 1. Autenticar no SUAP e obter token
            log.debug("Autenticando credenciais no SUAP");
            SuapTokenResponse tokenResponse = suapAuthClient.obtainToken(request);

            // Validar resposta de token
            securityValidator.validateTokenResponse(tokenResponse, request.username());

            log.debug("Token SUAP obtido com sucesso");

            // 2. Buscar dados do usuário no SUAP
            SuapUserData userData = fetchUserData(tokenResponse.access(), request.username());

            // Validar dados do usuário
            securityValidator.validateUserData(userData);

            // 3. Sincronizar dados SUAP
            syncSuapUserData(userData);

            // 4. Encontrar ou criar usuário no sistema IFMeetup
            User user = findOrCreateUser(userData);

            // 5. Atualizar último login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // 6. Gerar token JWT local
            String jwtToken = tokenProvider.createToken(user);
            UUID sessionId = tokenProvider.getSessionIdFromToken(jwtToken);
            LocalDateTime expiresAt = tokenProvider.getExpirationDateFromToken(jwtToken);

            // Log de sucesso para auditoria
            securityValidator.logLoginAttempt(request.username(), true, null);

            log.info("Login SUAP realizado com sucesso para usuário: {} - Session ID: {}",
                    user.getEmail(), sessionId);

            return AuthResponse.success(
                    "Login SUAP realizado com sucesso",
                    UserResponse.fromEntity(user),
                    sessionId,
                    expiresAt,
                    jwtToken);

        } catch (Exception e) {
            // Log de falha para auditoria
            securityValidator.logLoginAttempt(request.username(), false, e.getMessage());

            log.error("Erro durante login SUAP para matrícula {}: {}", request.username(), e.getMessage(), e);

            if (e instanceof AuthenticationException) {
                throw e;
            }

            throw new ExternalServiceException("Erro na comunicação com SUAP: " + e.getMessage());
        }
    }

    /**
     * Busca dados completos do usuário no SUAP usando token de acesso.
     * 
     * @param accessToken Token de acesso SUAP
     * @param matricula   Matrícula do usuário
     * @return Dados unificados do usuário SUAP
     */
    private SuapUserData fetchUserData(String accessToken, String matricula) {
        log.debug("Buscando dados do usuário no SUAP para matrícula: {}", matricula);

        String authHeader = "Bearer " + accessToken;

        try {
            // Tentar buscar como servidor primeiro
            log.debug("Tentando buscar como servidor");
            SuapServidoresResponse servidoresResponse = suapDataClient.getServidores(authHeader, matricula);

            if (servidoresResponse != null && servidoresResponse.results() != null
                    && !servidoresResponse.results().isEmpty()) {
                SuapServidorResponse servidor = servidoresResponse.results().get(0);
                log.debug("Usuário encontrado como servidor: {}", servidor.nome());
                return SuapUserData.fromServidor(servidor);
            }

        } catch (Exception e) {
            log.debug("Erro ao buscar como servidor (tentando como aluno): {}", e.getMessage());
        }

        try {
            // Tentar buscar como aluno
            log.debug("Tentando buscar como aluno");
            SuapAlunosResponse alunosResponse = suapDataClient.getAlunos(authHeader, matricula);

            if (alunosResponse != null && alunosResponse.results() != null && !alunosResponse.results().isEmpty()) {
                SuapAlunoResponse aluno = alunosResponse.results().get(0);
                log.debug("Usuário encontrado como aluno: {}", aluno.nome());
                return SuapUserData.fromAluno(aluno);
            }

        } catch (Exception e) {
            log.error("Erro ao buscar como aluno: {}", e.getMessage());
        }

        log.error("Usuário não encontrado no SUAP para matrícula: {}", matricula);
        throw new ResourceNotFoundException("Usuário não encontrado no SUAP");
    }

    /**
     * Encontra usuário existente ou cria novo baseado nos dados SUAP.
     * 
     * @param userData Dados do usuário SUAP
     * @return Usuário do sistema IFMeetup
     */
    private User findOrCreateUser(SuapUserData userData) {
        log.debug("Buscando ou criando usuário para email: {}", userData.generatedEmail());

        // Buscar usuário existente pelo email gerado
        return userRepository.findByEmail(userData.generatedEmail())
                .map(existingUser -> {
                    log.debug("Usuário existente encontrado, atualizando dados");
                    return updateExistingUser(existingUser, userData);
                })
                .orElseGet(() -> {
                    log.debug("Usuário não existe, criando novo");
                    return createNewUser(userData);
                });
    }

    /**
     * Atualiza usuário existente com dados mais recentes do SUAP.
     */
    private User updateExistingUser(User existingUser, SuapUserData userData) {
        log.debug("Atualizando usuário existente: {}", existingUser.getEmail());

        // Atualizar dados básicos
        existingUser.setFirstName(extractFirstName(userData.nome()));
        existingUser.setLastName(extractLastName(userData.nome()));

        // Verificar se precisa atualizar ProfileType
        ProfileType newProfileType = suapRoleMapper.mapToProfileType(userData);
        ProfileType currentProfileType = existingUser.getProfile() != null ? existingUser.getProfile().getProfileType()
                : null;

        if (currentProfileType != newProfileType) {
            log.info("Atualizando ProfileType de {} para {} para usuário: {}",
                    currentProfileType, newProfileType, existingUser.getEmail());

            // Log de atualização de perfil para auditoria
            securityValidator.logProfileUpdate(userData.matricula(),
                    currentProfileType != null ? currentProfileType.toString() : "null",
                    newProfileType.toString());

            updateUserProfile(existingUser, newProfileType);
            updateUserRoles(existingUser, newProfileType);
        }

        return userRepository.save(existingUser);
    }

    /**
     * Cria novo usuário no sistema baseado nos dados SUAP.
     */
    private User createNewUser(SuapUserData userData) {
        log.debug("Criando novo usuário para: {}", userData.generatedEmail());

        ProfileType profileType = suapRoleMapper.mapToProfileType(userData);
        Role defaultRole = getDefaultRoleForProfileType(profileType);

        User newUser = new User();
        newUser.setEmail(userData.generatedEmail());
        newUser.setFirstName(extractFirstName(userData.nome()));
        newUser.setLastName(extractLastName(userData.nome()));
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Senha aleatória
        newUser.setPhoneNumber(""); // Não disponível no SUAP
        newUser.setEmailVerified(true); // Usuários SUAP são considerados verificados
        newUser.setRoles(new HashSet<>(Set.of(defaultRole)));

        User savedUser = userRepository.save(newUser);

        // Criar perfil
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setProfileType(profileType);
        profile.setVerified(true);
        profile.setVerificationDate(LocalDateTime.now());

        userProfileRepository.save(profile);
        savedUser.setProfile(profile);

        // Log de criação de usuário para auditoria
        securityValidator.logUserCreation(userData.matricula(), userData.generatedEmail(), profileType.toString());

        log.info("Novo usuário SUAP criado: {} com perfil: {}", savedUser.getEmail(), profileType);

        return savedUser;
    }

    /**
     * Atualiza o perfil do usuário com novo ProfileType.
     */
    private void updateUserProfile(User user, ProfileType newProfileType) {
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            profile.setVerified(true);
            profile.setVerificationDate(LocalDateTime.now());
        }

        profile.setProfileType(newProfileType);
        userProfileRepository.save(profile);
        user.setProfile(profile);
    }

    /**
     * Atualiza as roles do usuário baseado no novo ProfileType.
     */
    private void updateUserRoles(User user, ProfileType profileType) {
        Role newRole = getDefaultRoleForProfileType(profileType);
        user.setRoles(new HashSet<>(Set.of(newRole)));
    }

    /**
     * Obtém role padrão para o ProfileType.
     */
    private Role getDefaultRoleForProfileType(ProfileType profileType) {
        String roleName = switch (profileType) {
            case ADMIN -> "ADMIN";
            case COORDINATOR -> "COORDINATOR";
            case TEACHER -> "TEACHER";
            case STUDENT -> "STUDENT";
        };

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));
    }

    /**
     * Extrai primeiro nome do nome completo.
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Usuário";
        }

        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extrai último nome do nome completo.
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "SUAP";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }

        return "SUAP";
    }

    /**
     * Sincroniza dados SUAP na entidade SuapUser.
     * Cria nova entidade ou atualiza existente com dados mais recentes.
     * 
     * @param userData Dados atualizados do usuário SUAP
     */
    private void syncSuapUserData(SuapUserData userData) {
        log.debug("Sincronizando dados SUAP para matrícula: {}", userData.matricula());

        SuapUser suapUser = suapUserRepository.findByMatricula(userData.matricula())
                .orElse(null);

        if (suapUser == null) {
            // Criar nova entidade SuapUser
            log.debug("Criando nova entidade SuapUser para matrícula: {}", userData.matricula());
            suapUser = createNewSuapUser(userData);
        } else {
            // Atualizar entidade existente
            log.debug("Atualizando entidade SuapUser existente para matrícula: {}", userData.matricula());
            updateSuapUser(suapUser, userData);
        }

        suapUserRepository.save(suapUser);
        log.debug("Dados SUAP sincronizados com sucesso para matrícula: {}", userData.matricula());
    }

    /**
     * Cria nova entidade SuapUser com dados do SUAP.
     */
    private SuapUser createNewSuapUser(SuapUserData userData) {
        SuapUser suapUser = new SuapUser();
        updateSuapUserFields(suapUser, userData);
        return suapUser;
    }

    /**
     * Atualiza entidade SuapUser existente com novos dados.
     */
    private void updateSuapUser(SuapUser suapUser, SuapUserData userData) {
        updateSuapUserFields(suapUser, userData);
    }

    /**
     * Atualiza campos da entidade SuapUser com dados do SUAP.
     */
    private void updateSuapUserFields(SuapUser suapUser, SuapUserData userData) {
        suapUser.setMatricula(userData.matricula());
        suapUser.setNome(userData.nome());
        suapUser.setUserType(userData.userType());
        suapUser.setSuapUuid(userData.uuid());
        suapUser.setGeneratedEmail(userData.generatedEmail());

        // Campos específicos de servidor
        suapUser.setCargoEmprego(userData.cargoEmprego());
        suapUser.setFuncaoCodigo(userData.funcaoCodigo());
        suapUser.setSetorExercicio(userData.setorExercicio());
        suapUser.setSituacao(userData.situacao());

        // Campos específicos de aluno
        suapUser.setCurso(userData.curso());
        suapUser.setSituacaoAluno(userData.situacaoAluno());

        // Atualizar timestamp de sincronização
        suapUser.updateLastSync();
    }

    /**
     * Verifica e atualiza ProfileType do usuário baseado nos dados SUAP mais
     * recentes.
     * Este método é chamado durante a sincronização para garantir que mudanças
     * de função ou cargo no SUAP sejam refletidas no sistema.
     * 
     * @param user     Usuário do sistema
     * @param userData Dados atualizados do SUAP
     * @return true se o ProfileType foi alterado
     */
    private boolean verifyAndUpdateProfileType(User user, SuapUserData userData) {
        log.debug("Verificando necessidade de atualização de ProfileType para usuário: {}", user.getEmail());

        ProfileType currentProfileType = user.getProfile() != null ? user.getProfile().getProfileType() : null;
        ProfileType newProfileType = suapRoleMapper.mapToProfileType(userData);

        if (currentProfileType != newProfileType) {
            log.info("ProfileType alterado de {} para {} para usuário: {} baseado em dados SUAP atualizados",
                    currentProfileType, newProfileType, user.getEmail());

            updateUserProfile(user, newProfileType);
            updateUserRoles(user, newProfileType);

            return true;
        }

        log.debug("ProfileType não necessita atualização para usuário: {}", user.getEmail());
        return false;
    }

    /**
     * Sincroniza dados SUAP para usuário específico.
     * Método público para sincronização sob demanda ou periódica.
     * 
     * @param matricula   Matrícula do usuário para sincronizar
     * @param accessToken Token de acesso SUAP válido
     * @return true se sincronização foi bem-sucedida
     */
    @Transactional
    public boolean syncUserData(String matricula, String accessToken) {
        log.info("Iniciando sincronização de dados SUAP para matrícula: {}", matricula);

        try {
            // Buscar dados atualizados no SUAP
            SuapUserData userData = fetchUserData(accessToken, matricula);

            // Sincronizar entidade SuapUser
            syncSuapUserData(userData);

            // Verificar se existe usuário no sistema e atualizar se necessário
            userRepository.findByEmail(userData.generatedEmail())
                    .ifPresent(user -> {
                        log.debug("Atualizando usuário existente durante sincronização: {}", user.getEmail());
                        updateExistingUser(user, userData);
                    });

            log.info("Sincronização concluída com sucesso para matrícula: {}", matricula);
            return true;

        } catch (Exception e) {
            log.error("Erro durante sincronização para matrícula {}: {}", matricula, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sincroniza dados de múltiplos usuários que precisam de atualização.
     * Método para sincronização em lote de usuários com dados desatualizados.
     * 
     * @param accessToken Token de acesso SUAP válido
     * @return Número de usuários sincronizados com sucesso
     */
    @Transactional
    public int syncOutdatedUsers(String accessToken) {
        log.info("Iniciando sincronização em lote de usuários desatualizados");

        // Buscar usuários que precisam de sincronização (dados com mais de 24h)
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        var outdatedUsers = suapUserRepository.findUsersNeedingSync(cutoffTime);

        log.info("Encontrados {} usuários que precisam de sincronização", outdatedUsers.size());

        int successCount = 0;
        for (SuapUser suapUser : outdatedUsers) {
            try {
                if (syncUserData(suapUser.getMatricula(), accessToken)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.warn("Falha na sincronização para matrícula {}: {}",
                        suapUser.getMatricula(), e.getMessage());
            }
        }

        log.info("Sincronização em lote concluída: {}/{} usuários sincronizados com sucesso",
                successCount, outdatedUsers.size());

        return successCount;
    }
}