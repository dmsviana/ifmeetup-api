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
 * serviço responsável pela autenticação de usuários SUAP
 * 
 * implementa o fluxo completo de:
 * - autenticação no SUAP
 * - busca de dados do usuário
 * - criação/atualização de usuários no sistema
 * - registro de dados SUAP para auditoria
 * - geração de tokens JWT locais
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
     * faz login completo via SUAP orquestrando todo o fluxo
     * 
     * @param request  credenciais SUAP (matrícula e senha)
     * @param response response HTTP para configuração de cookies
     * @return resposta de autenticação com token JWT do IFMeetup
     */
    @Transactional
    public AuthResponse loginWithSuap(SuapLoginRequest request, HttpServletResponse response) {
        log.info("Iniciando login SUAP para matrícula: {}", request.username());

        try {
            securityValidator.validateCredentials(request);

            log.debug("Autenticando credenciais no SUAP");
            SuapTokenResponse tokenResponse = suapAuthClient.obtainToken(request);

            securityValidator.validateTokenResponse(tokenResponse, request.username());

            log.debug("Token SUAP obtido com sucesso");

            SuapUserData userData = fetchUserData(tokenResponse.access(), request.username());

            securityValidator.validateUserData(userData);

            saveSuapUserData(userData);

            User user = findOrCreateUser(userData);

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String jwtToken = tokenProvider.createToken(user);
            UUID sessionId = tokenProvider.getSessionIdFromToken(jwtToken);
            LocalDateTime expiresAt = tokenProvider.getExpirationDateFromToken(jwtToken);

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
            securityValidator.logLoginAttempt(request.username(), false, e.getMessage());

            log.error("Erro durante login SUAP para matrícula {}: {}", request.username(), e.getMessage(), e);

            if (e instanceof AuthenticationException) {
                throw e;
            }

            throw new ExternalServiceException("Erro na comunicação com SUAP: " + e.getMessage());
        }
    }

    /**
     * busca dados completos do usuário no SUAP usando token de acesso
     * 
     * @param accessToken token de acesso SUAP
     * @param matricula   matrícula do usuário
     * @return dados unificados do usuário SUAP
     */
    private SuapUserData fetchUserData(String accessToken, String matricula) {
        log.debug("Buscando dados do usuário no SUAP para matrícula: {}", matricula);

        String authHeader = "Bearer " + accessToken;

        try {
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
     * encontra usuário existente ou cria novo baseado nos dados SUAP
     * 
     * @param userData dados do usuário SUAP
     * @return usuário do sistema IFMeetup
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
     * atualiza usuário existente com dados mais recentes do SUAP
     */
    private User updateExistingUser(User existingUser, SuapUserData userData) {
        log.debug("Atualizando usuário existente: {}", existingUser.getEmail());

        existingUser.setFirstName(extractFirstName(userData.nome()));
        existingUser.setLastName(extractLastName(userData.nome()));

        ProfileType newProfileType = suapRoleMapper.mapToProfileType(userData);
        ProfileType currentProfileType = existingUser.getProfile() != null ? existingUser.getProfile().getProfileType()
                : null;

        if (currentProfileType != newProfileType) {
            log.info("Atualizando ProfileType de {} para {} para usuário: {}",
                    currentProfileType, newProfileType, existingUser.getEmail());

            securityValidator.logProfileUpdate(userData.matricula(),
                    currentProfileType != null ? currentProfileType.toString() : "null",
                    newProfileType.toString());

            updateUserProfile(existingUser, newProfileType);
            updateUserRoles(existingUser, newProfileType);
        }

        return userRepository.save(existingUser);
    }

    /**
     * cria novo usuário no sistema baseado nos dados SUAP
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

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setProfileType(profileType);
        profile.setVerified(true);
        profile.setVerificationDate(LocalDateTime.now());

        userProfileRepository.save(profile);
        savedUser.setProfile(profile);

        securityValidator.logUserCreation(userData.matricula(), userData.generatedEmail(), profileType.toString());

        log.info("Novo usuário SUAP criado: {} com perfil: {}", savedUser.getEmail(), profileType);

        return savedUser;
    }

    /**
     * atualiza o perfil do usuário com novo ProfileType
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
     * atualiza as roles do usuário baseado no novo ProfileType
     */
    private void updateUserRoles(User user, ProfileType profileType) {
        Role newRole = getDefaultRoleForProfileType(profileType);
        user.setRoles(new HashSet<>(Set.of(newRole)));
    }

    /**
     * obtém role padrão para o ProfileType
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
     * extrai primeiro nome do nome completo
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Usuário";
        }

        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * extrai último nome do nome completo
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
     * salva dados SUAP na entidade SuapUser para auditoria
     * cria nova entidade ou atualiza existente com dados mais recentes
     * 
     * @param userData dados do usuário SUAP
     */
    private void saveSuapUserData(SuapUserData userData) {
        log.debug("salvando dados SUAP para matrícula: {}", userData.matricula());

        SuapUser suapUser = suapUserRepository.findByMatricula(userData.matricula())
                .orElse(null);

        if (suapUser == null) {
            log.debug("criando nova entidade SuapUser para matrícula: {}", userData.matricula());
            suapUser = createNewSuapUser(userData);
        } else {
            log.debug("atualizando entidade SuapUser existente para matrícula: {}", userData.matricula());
            updateSuapUser(suapUser, userData);
        }

        suapUserRepository.save(suapUser);
        log.debug("dados SUAP salvos com sucesso para matrícula: {}", userData.matricula());
    }

    /**
     * cria nova entidade SuapUser com dados do SUAP
     */
    private SuapUser createNewSuapUser(SuapUserData userData) {
        SuapUser suapUser = new SuapUser();
        updateSuapUserFields(suapUser, userData);
        return suapUser;
    }

    /**
     * atualiza entidade SuapUser existente com novos dados
     */
    private void updateSuapUser(SuapUser suapUser, SuapUserData userData) {
        updateSuapUserFields(suapUser, userData);
    }

    /**
     * atualiza campos da entidade SuapUser com dados do SUAP
     */
    private void updateSuapUserFields(SuapUser suapUser, SuapUserData userData) {
        suapUser.setMatricula(userData.matricula());
        suapUser.setNome(userData.nome());
        suapUser.setUserType(userData.userType());
        suapUser.setSuapUuid(userData.uuid());
        suapUser.setGeneratedEmail(userData.generatedEmail());

        suapUser.setCargoEmprego(userData.cargoEmprego());
        suapUser.setFuncaoCodigo(userData.funcaoCodigo());
        suapUser.setSetorExercicio(userData.setorExercicio());
        suapUser.setSituacao(userData.situacao());

        suapUser.setCurso(userData.curso());
        suapUser.setSituacaoAluno(userData.situacaoAluno());
    }

}