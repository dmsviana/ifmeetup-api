package br.edu.ifpb.ifmeetup.integration.suap.util;

/**
 * Utilitário para mensagens de erro padronizadas da integração SUAP.
 * Fornece mensagens amigáveis ao usuário para diferentes tipos de erro.
 */
public final class SuapErrorMessages {
    
    private SuapErrorMessages() {
        // Utility class - não deve ser instanciada
    }
    
    // Mensagens de autenticação
    public static final String INVALID_CREDENTIALS = 
            "Matrícula ou senha incorreta. Verifique suas credenciais SUAP e tente novamente.";
    
    public static final String INVALID_TOKEN = 
            "Sessão SUAP expirada. Faça login novamente.";
    
    public static final String TOKEN_REFRESH_FAILURE = 
            "Não foi possível renovar a sessão SUAP. Faça login novamente.";
    
    // Mensagens de dados não encontrados
    public static final String USER_NOT_FOUND = 
            "Usuário não encontrado no SUAP. Verifique se sua matrícula está correta.";
    
    public static final String SERVIDOR_NOT_FOUND = 
            "Servidor não encontrado no SUAP. Verifique se você é um servidor ativo da instituição.";
    
    public static final String ALUNO_NOT_FOUND = 
            "Aluno não encontrado no SUAP. Verifique se você é um aluno ativo da instituição.";
    
    public static final String INCOMPLETE_DATA = 
            "Dados incompletos retornados pelo SUAP. Entre em contato com o suporte técnico.";
    
    // Mensagens de serviço indisponível
    public static final String SERVICE_TIMEOUT = 
            "O SUAP está demorando para responder. Tente novamente em alguns minutos.";
    
    public static final String INTERNAL_ERROR = 
            "O SUAP está com problemas internos. Tente novamente mais tarde.";
    
    public static final String TEMPORARILY_UNAVAILABLE = 
            "O SUAP está temporariamente indisponível. Tente novamente em alguns minutos.";
    
    public static final String CONNECTIVITY_FAILURE = 
            "Não foi possível conectar ao SUAP. Verifique sua conexão com a internet e tente novamente.";
    
    // Mensagens genéricas
    public static final String INTEGRATION_ERROR = 
            "Ocorreu um erro na integração com o SUAP. Tente novamente ou entre em contato com o suporte.";
    
    public static final String MAINTENANCE_MODE = 
            "O SUAP está em manutenção. Tente fazer login mais tarde ou use o login por email/senha.";
    
    /**
     * Retorna uma mensagem amigável baseada no código de erro.
     * 
     * @param errorCode código de erro da exceção
     * @return mensagem amigável ao usuário
     */
    public static String getUserFriendlyMessage(String errorCode) {
        if (errorCode == null) {
            return INTEGRATION_ERROR;
        }
        
        return switch (errorCode) {
            case "SUAP_INVALID_CREDENTIALS" -> INVALID_CREDENTIALS;
            case "SUAP_INVALID_TOKEN" -> INVALID_TOKEN;
            case "SUAP_TOKEN_REFRESH_FAILURE" -> TOKEN_REFRESH_FAILURE;
            case "SUAP_USER_NOT_FOUND" -> USER_NOT_FOUND;
            case "SUAP_SERVIDOR_NOT_FOUND" -> SERVIDOR_NOT_FOUND;
            case "SUAP_ALUNO_NOT_FOUND" -> ALUNO_NOT_FOUND;
            case "SUAP_INCOMPLETE_DATA" -> INCOMPLETE_DATA;
            case "SUAP_TIMEOUT" -> SERVICE_TIMEOUT;
            case "SUAP_INTERNAL_ERROR" -> INTERNAL_ERROR;
            case "SUAP_TEMPORARILY_UNAVAILABLE" -> TEMPORARILY_UNAVAILABLE;
            case "SUAP_CONNECTIVITY_FAILURE" -> CONNECTIVITY_FAILURE;
            default -> INTEGRATION_ERROR;
        };
    }
    
    /**
     * Retorna uma mensagem de fallback para login alternativo.
     * 
     * @return mensagem sugerindo login por email/senha
     */
    public static String getAlternativeLoginMessage() {
        return "Você pode tentar fazer login usando seu email e senha cadastrados no sistema.";
    }
    
    /**
     * Retorna uma mensagem completa com sugestão de login alternativo.
     * 
     * @param errorCode código de erro da exceção
     * @return mensagem completa com sugestão
     */
    public static String getCompleteErrorMessage(String errorCode) {
        String mainMessage = getUserFriendlyMessage(errorCode);
        String alternativeMessage = getAlternativeLoginMessage();
        
        return mainMessage + " " + alternativeMessage;
    }
}