package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção para quando dados de usuário não são encontrados no SUAP.
 * Usada quando um usuário não é encontrado nas APIs de dados do SUAP.
 */
public class SuapDataNotFoundException extends SuapIntegrationException {
    
    private static final String DEFAULT_ERROR_CODE = "SUAP_DATA_NOT_FOUND";
    
    public SuapDataNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public SuapDataNotFoundException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public SuapDataNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public SuapDataNotFoundException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public SuapDataNotFoundException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    public SuapDataNotFoundException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, errorCode, details, cause);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public SuapDataNotFoundException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Override do método addDetails para retornar o tipo correto.
     */
    @Override
    public SuapDataNotFoundException addDetails(Map<String, Object> details) {
        super.addDetails(details);
        return this;
    }
    
    /**
     * Cria uma exceção para usuário não encontrado no SUAP.
     * 
     * @param matricula a matrícula que não foi encontrada
     * @param userType o tipo de usuário buscado (SERVIDOR ou ALUNO)
     * @return nova instância de SuapDataNotFoundException
     */
    public static SuapDataNotFoundException userNotFound(String matricula, String userType) {
        return new SuapDataNotFoundException(
                "Usuário não encontrado no SUAP", 
                "SUAP_USER_NOT_FOUND")
                .addDetail("matricula", matricula)
                .addDetail("userType", userType);
    }
    
    /**
     * Cria uma exceção para servidor não encontrado no SUAP.
     * 
     * @param matricula a matrícula do servidor que não foi encontrada
     * @return nova instância de SuapDataNotFoundException
     */
    public static SuapDataNotFoundException servidorNotFound(String matricula) {
        return new SuapDataNotFoundException(
                "Servidor não encontrado no SUAP", 
                "SUAP_SERVIDOR_NOT_FOUND")
                .addDetail("matricula", matricula);
    }
    
    /**
     * Cria uma exceção para aluno não encontrado no SUAP.
     * 
     * @param matricula a matrícula do aluno que não foi encontrada
     * @return nova instância de SuapDataNotFoundException
     */
    public static SuapDataNotFoundException alunoNotFound(String matricula) {
        return new SuapDataNotFoundException(
                "Aluno não encontrado no SUAP", 
                "SUAP_ALUNO_NOT_FOUND")
                .addDetail("matricula", matricula);
    }
    
    /**
     * Cria uma exceção para dados incompletos retornados pelo SUAP.
     * 
     * @param matricula a matrícula do usuário
     * @param missingFields campos que estão faltando
     * @return nova instância de SuapDataNotFoundException
     */
    public static SuapDataNotFoundException incompleteData(String matricula, String... missingFields) {
        return new SuapDataNotFoundException(
                "Dados incompletos retornados pelo SUAP", 
                "SUAP_INCOMPLETE_DATA")
                .addDetail("matricula", matricula)
                .addDetail("missingFields", String.join(", ", missingFields));
    }
}