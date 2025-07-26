package br.edu.ifpb.ifmeetup.integration.suap.service;

import br.edu.ifpb.ifmeetup.exception.ValidationException;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapLoginRequest;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapTokenResponse;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapUserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Validador de segurança para operações SUAP.
 * 
 * Responsável por:
 * - Validação de credenciais de entrada
 * - Validação de respostas da API SUAP
 * - Validação de dados de usuário
 * - Logs de segurança detalhados
 */
@Slf4j
@Component
public class SuapSecurityValidator {
    
    // Padrão para validação de matrícula (7-12 dígitos)
    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^\\d{7,12}$");
    
    // Tempo mínimo de expiração de token (em segundos)
    private static final long MIN_TOKEN_EXPIRY_SECONDS = 300; // 5 minutos
    
    /**
     * Valida credenciais SUAP antes de enviar para autenticação.
     * 
     * @param request Requisição de login SUAP
     * @throws ValidationException Se credenciais são inválidas
     */
    public void validateCredentials(SuapLoginRequest request) {
        log.debug("Validando credenciais SUAP para matrícula: {}", request.username());
        
        // Validar se username não é nulo ou vazio
        if (request.username() == null || request.username().trim().isEmpty()) {
            log.warn("Tentativa de login com username vazio");
            throw ValidationException.forField("username", request.username(), 
                "Username é obrigatório");
        }
        
        // Validar se password não é nulo ou vazio
        if (request.password() == null || request.password().trim().isEmpty()) {
            log.warn("Tentativa de login com password vazio para matrícula: {}", request.username());
            throw ValidationException.forField("password", null, 
                "Password é obrigatório");
        }
        
        // Validar formato da matrícula
        if (!isValidMatriculaFormat(request.username())) {
            log.warn("Tentativa de login com formato de matrícula inválido: {}", request.username());
            throw ValidationException.forField("username", request.username(), 
                "Matrícula deve conter entre 7 e 12 dígitos");
        }
        
        // Validar comprimento mínimo da senha
        if (request.password().trim().length() < 3) {
            log.warn("Tentativa de login com senha muito curta para matrícula: {}", request.username());
            throw ValidationException.forField("password", null, 
                "Password deve ter pelo menos 3 caracteres");
        }
        
        log.debug("Credenciais SUAP validadas com sucesso para matrícula: {}", request.username());
    }
    
    /**
     * Valida resposta de token recebida do SUAP.
     * 
     * @param tokenResponse Resposta de token do SUAP
     * @param matricula Matrícula do usuário (para logs)
     * @throws ValidationException Se resposta de token é inválida
     */
    public void validateTokenResponse(SuapTokenResponse tokenResponse, String matricula) {
        log.debug("Validando resposta de token SUAP para matrícula: {}", matricula);
        
        if (tokenResponse == null) {
            log.error("Resposta de token SUAP é null para matrícula: {}", matricula);
            throw ValidationException.forField("tokenResponse", null, 
                "Resposta de token SUAP é inválida");
        }
        
        // Validar se access token está presente
        if (tokenResponse.access() == null || tokenResponse.access().trim().isEmpty()) {
            log.error("Access token ausente na resposta SUAP para matrícula: {}", matricula);
            throw ValidationException.forField("access", tokenResponse.access(), 
                "Access token é obrigatório");
        }
        
        // Validar se refresh token está presente
        if (tokenResponse.refresh() == null || tokenResponse.refresh().trim().isEmpty()) {
            log.warn("Refresh token ausente na resposta SUAP para matrícula: {}", matricula);
        }
        
        // Validar tempo de expiração
        if (tokenResponse.accessExpiresIn() != null) {
            if (tokenResponse.accessExpiresIn() < MIN_TOKEN_EXPIRY_SECONDS) {
                log.warn("Token SUAP com tempo de expiração muito baixo ({} segundos) para matrícula: {}", 
                        tokenResponse.accessExpiresIn(), matricula);
            }
            
            if (tokenResponse.accessExpiresIn() <= 0) {
                log.error("Token SUAP já expirado para matrícula: {}", matricula);
                throw ValidationException.forField("accessExpiresIn", tokenResponse.accessExpiresIn(), 
                    "Token já está expirado");
            }
        }
        
        // Validar formato básico do JWT (deve ter 3 partes separadas por ponto)
        String[] tokenParts = tokenResponse.access().split("\\.");
        if (tokenParts.length != 3) {
            log.error("Formato de token JWT inválido para matrícula: {}", matricula);
            throw ValidationException.forField("access", null, 
                "Formato de token JWT inválido");
        }
        
        log.debug("Resposta de token SUAP validada com sucesso para matrícula: {}", matricula);
        log.info("Token SUAP obtido com sucesso para matrícula: {} - Expira em: {} segundos", 
                matricula, tokenResponse.accessExpiresIn());
    }
    
    /**
     * Valida dados de usuário recebidos do SUAP.
     * 
     * @param userData Dados do usuário SUAP
     * @throws ValidationException Se dados do usuário são inválidos
     */
    public void validateUserData(SuapUserData userData) {
        log.debug("Validando dados de usuário SUAP para matrícula: {}", 
                userData != null ? userData.matricula() : "null");
        
        if (userData == null) {
            log.error("Dados de usuário SUAP são null");
            throw ValidationException.forField("userData", null, 
                "Dados de usuário SUAP são obrigatórios");
        }
        
        // Validar campos essenciais
        validateEssentialUserFields(userData);
        
        // Validar dados específicos por tipo de usuário
        if (userData.isServidor()) {
            validateServidorData(userData);
        } else if (userData.isAluno()) {
            validateAlunoData(userData);
        } else {
            log.error("Tipo de usuário SUAP não reconhecido: {}", userData.userType());
            throw ValidationException.forField("userType", userData.userType(), 
                "Tipo de usuário não reconhecido");
        }
        
        log.debug("Dados de usuário SUAP validados com sucesso para matrícula: {}", userData.matricula());
        log.info("Dados de usuário SUAP validados: matrícula={}, nome={}, tipo={}", 
                userData.matricula(), userData.nome(), userData.userType());
    }
    
    /**
     * Valida campos essenciais presentes em todos os tipos de usuário.
     */
    private void validateEssentialUserFields(SuapUserData userData) {
        // Validar UUID
        if (userData.uuid() == null || userData.uuid().trim().isEmpty()) {
            log.error("UUID ausente nos dados SUAP para matrícula: {}", userData.matricula());
            throw ValidationException.forField("uuid", userData.uuid(), 
                "UUID do usuário é obrigatório");
        }
        
        // Validar nome
        if (userData.nome() == null || userData.nome().trim().isEmpty()) {
            log.error("Nome ausente nos dados SUAP para matrícula: {}", userData.matricula());
            throw ValidationException.forField("nome", userData.nome(), 
                "Nome do usuário é obrigatório");
        }
        
        if (userData.nome().trim().length() < 2) {
            log.error("Nome muito curto nos dados SUAP para matrícula: {}", userData.matricula());
            throw ValidationException.forField("nome", userData.nome(), 
                "Nome deve ter pelo menos 2 caracteres");
        }
        
        // Validar matrícula
        if (userData.matricula() == null || userData.matricula().trim().isEmpty()) {
            log.error("Matrícula ausente nos dados SUAP");
            throw ValidationException.forField("matricula", userData.matricula(), 
                "Matrícula é obrigatória");
        }
        
        if (!isValidMatriculaFormat(userData.matricula())) {
            log.error("Formato de matrícula inválido nos dados SUAP: {}", userData.matricula());
            throw ValidationException.forField("matricula", userData.matricula(), 
                "Formato de matrícula inválido");
        }
        
        // Validar userType
        if (userData.userType() == null) {
            log.error("Tipo de usuário ausente nos dados SUAP para matrícula: {}", userData.matricula());
            throw ValidationException.forField("userType", null, 
                "Tipo de usuário é obrigatório");
        }
        
        // Validar email gerado
        if (userData.generatedEmail() == null || userData.generatedEmail().trim().isEmpty()) {
            log.error("Email gerado ausente nos dados SUAP para matrícula: {}", userData.matricula());
            throw ValidationException.forField("generatedEmail", userData.generatedEmail(), 
                "Email gerado é obrigatório");
        }
    }
    
    /**
     * Valida dados específicos de servidor.
     */
    private void validateServidorData(SuapUserData userData) {
        log.debug("Validando dados específicos de servidor para matrícula: {}", userData.matricula());
        
        // Para servidores, cargo é importante para mapeamento de perfil
        if (userData.cargoEmprego() == null || userData.cargoEmprego().trim().isEmpty()) {
            log.warn("Cargo de emprego ausente para servidor - matrícula: {}", userData.matricula());
        }
        
        // Situação é importante para verificar se servidor está ativo
        if (userData.situacao() == null || userData.situacao().trim().isEmpty()) {
            log.warn("Situação ausente para servidor - matrícula: {}", userData.matricula());
        }
        
        log.debug("Dados de servidor validados para matrícula: {}", userData.matricula());
    }
    
    /**
     * Valida dados específicos de aluno.
     */
    private void validateAlunoData(SuapUserData userData) {
        log.debug("Validando dados específicos de aluno para matrícula: {}", userData.matricula());
        
        // Para alunos, situação é importante para verificar se está matriculado
        if (userData.situacaoAluno() == null || userData.situacaoAluno().trim().isEmpty()) {
            log.warn("Situação de aluno ausente para matrícula: {}", userData.matricula());
        }
        
        // Curso é importante para contexto do aluno
        if (userData.curso() == null || userData.curso().trim().isEmpty()) {
            log.warn("Curso ausente para aluno - matrícula: {}", userData.matricula());
        }
        
        log.debug("Dados de aluno validados para matrícula: {}", userData.matricula());
    }
    
    /**
     * Valida formato da matrícula usando regex.
     * 
     * @param matricula Matrícula a ser validada
     * @return true se formato é válido
     */
    public boolean isValidMatriculaFormat(String matricula) {
        if (matricula == null) {
            return false;
        }
        
        String cleanMatricula = matricula.trim();
        boolean isValid = MATRICULA_PATTERN.matcher(cleanMatricula).matches();
        
        if (!isValid) {
            log.debug("Formato de matrícula inválido: {} (deve conter 7-12 dígitos)", matricula);
        }
        
        return isValid;
    }
    
    /**
     * Registra evento de segurança para auditoria.
     * 
     * @param event Tipo do evento
     * @param matricula Matrícula do usuário
     * @param details Detalhes adicionais
     */
    public void logSecurityEvent(String event, String matricula, String details) {
        log.info("SECURITY_EVENT: {} - Matrícula: {} - Detalhes: {} - Timestamp: {}", 
                event, matricula, details, LocalDateTime.now());
    }
    
    /**
     * Registra tentativa de login para auditoria.
     * 
     * @param matricula Matrícula do usuário
     * @param success Se login foi bem-sucedido
     * @param errorMessage Mensagem de erro (se aplicável)
     */
    public void logLoginAttempt(String matricula, boolean success, String errorMessage) {
        if (success) {
            log.info("LOGIN_SUCCESS: Matrícula: {} - Timestamp: {}", matricula, LocalDateTime.now());
        } else {
            log.warn("LOGIN_FAILURE: Matrícula: {} - Erro: {} - Timestamp: {}", 
                    matricula, errorMessage, LocalDateTime.now());
        }
    }
    
    /**
     * Registra criação de usuário para auditoria.
     * 
     * @param matricula Matrícula do usuário
     * @param email Email gerado
     * @param profileType Tipo de perfil atribuído
     */
    public void logUserCreation(String matricula, String email, String profileType) {
        log.info("USER_CREATION: Matrícula: {} - Email: {} - Perfil: {} - Timestamp: {}", 
                matricula, email, profileType, LocalDateTime.now());
    }
    
    /**
     * Registra atualização de perfil para auditoria.
     * 
     * @param matricula Matrícula do usuário
     * @param oldProfile Perfil anterior
     * @param newProfile Novo perfil
     */
    public void logProfileUpdate(String matricula, String oldProfile, String newProfile) {
        log.info("PROFILE_UPDATE: Matrícula: {} - De: {} - Para: {} - Timestamp: {}", 
                matricula, oldProfile, newProfile, LocalDateTime.now());
    }
}