package br.edu.ifpb.ifmeetup.integration.suap.mapper;

import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapUserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mapper responsável por mapear dados do SUAP para ProfileType do sistema IFMeetup.
 * Implementa as regras de negócio específicas para determinação de perfis baseado
 * em funções e cargos do IFPB.
 */
@Component
public class SuapRoleMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(SuapRoleMapper.class);
    
    // Palavras-chave para identificação de cargos de professor
    private static final String[] PROFESSOR_KEYWORDS = {"PROFESSOR"};
    
    // Palavras-chave para identificação de cargos administrativos de coordenação
    private static final String[] ADMINISTRATIVE_COORDINATOR_KEYWORDS = {
        "DIRETOR", "COORDENADOR"
    };
    
    // Palavras-chave para identificação de setores de coordenação
    private static final String[] COORDINATION_SECTOR_KEYWORDS = {
        "COORD", "DIRETOR"
    };
    
    /**
     * Mapeia dados do SUAP para ProfileType baseado nas regras de negócio.
     * 
     * @param userData Dados unificados do usuário SUAP
     * @return ProfileType correspondente às funções do usuário
     */
    public ProfileType mapToProfileType(SuapUserData userData) {
        if (userData == null) {
            logger.warn("SuapUserData é null, retornando ProfileType.TEACHER como padrão");
            return ProfileType.TEACHER;
        }
        
        logger.debug("Iniciando mapeamento de ProfileType para usuário: matricula={}, userType={}", 
                    userData.matricula(), userData.userType());
        
        // Regra 1: Alunos sempre recebem ProfileType.STUDENT
        if (userData.userType() == SuapUserType.ALUNO) {
            logger.debug("Usuário é ALUNO, mapeando para ProfileType.STUDENT");
            return ProfileType.STUDENT;
        }
        
        // Para servidores, aplicar regras específicas
        if (userData.userType() == SuapUserType.SERVIDOR) {
            return mapServidorToProfileType(userData);
        }
        
        // Fallback para casos não previstos
        logger.warn("UserType não reconhecido: {}, retornando ProfileType.TEACHER como padrão", 
                   userData.userType());
        return ProfileType.TEACHER;
    }
    
    /**
     * Mapeia servidor para ProfileType baseado em cargo e função.
     */
    private ProfileType mapServidorToProfileType(SuapUserData userData) {
        String cargoEmprego = userData.cargoEmprego();
        Integer funcaoCodigo = userData.funcaoCodigo();
        String setorExercicio = userData.setorExercicio();
        
        logger.debug("Mapeando servidor: cargo={}, funcaoCodigo={}, setor={}", 
                    cargoEmprego, funcaoCodigo, setorExercicio);
        
        // Regra 2: Verificar se é cargo administrativo de coordenação
        if (isAdministrativeCoordinator(cargoEmprego, setorExercicio)) {
            logger.debug("Servidor identificado como coordenador administrativo");
            return ProfileType.COORDINATOR;
        }
        
        // Regra 3: Verificar se é professor
        if (isProfessor(cargoEmprego)) {
            // Regra 3a: Professor com função de coordenação
            if (isCoordinator(funcaoCodigo, setorExercicio)) {
                logger.debug("Professor identificado como coordenador por função ou setor");
                return ProfileType.COORDINATOR;
            }
            
            // Regra 3b: Professor sem função de coordenação
            logger.debug("Professor identificado como teacher (sem função de coordenação)");
            return ProfileType.TEACHER;
        }
        
        // Regra 4: Padrão para servidores que não se enquadram nas regras anteriores
        logger.debug("Servidor não se enquadra em regras específicas, aplicando padrão ProfileType.TEACHER");
        return ProfileType.TEACHER;
    }
    
    /**
     * Verifica se o servidor possui função de coordenação baseado em:
     * - Código de função diferente de null
     * - Setor de exercício contendo palavras-chave de coordenação
     * 
     * @param funcaoCodigo Código da função do servidor
     * @param setorExercicio Nome do setor de exercício
     * @return true se possui função de coordenação
     */
    public boolean isCoordinator(Integer funcaoCodigo, String setorExercicio) {
        logger.debug("Verificando função de coordenação: funcaoCodigo={}, setorExercicio={}", 
                    funcaoCodigo, setorExercicio);
        
        // Critério 1: Possui código de função (indica função específica)
        if (funcaoCodigo != null) {
            logger.debug("Servidor possui funcaoCodigo={}, considerado coordenador", funcaoCodigo);
            return true;
        }
        
        // Critério 2: Setor de exercício contém palavras-chave de coordenação
        if (setorExercicio != null && !setorExercicio.trim().isEmpty()) {
            String setorUpper = setorExercicio.toUpperCase();
            
            for (String keyword : COORDINATION_SECTOR_KEYWORDS) {
                if (setorUpper.contains(keyword)) {
                    logger.debug("Setor de exercício '{}' contém palavra-chave de coordenação '{}'", 
                               setorExercicio, keyword);
                    return true;
                }
            }
        }
        
        logger.debug("Servidor não possui função de coordenação");
        return false;
    }
    
    /**
     * Verifica se o servidor possui cargo administrativo de coordenação baseado em:
     * - Cargo contendo palavras-chave administrativas
     * - Setor contendo "COORD"
     * 
     * @param cargoEmprego Cargo/emprego do servidor
     * @param setorExercicio Nome do setor de exercício
     * @return true se possui cargo administrativo de coordenação
     */
    public boolean isAdministrativeCoordinator(String cargoEmprego, String setorExercicio) {
        logger.debug("Verificando cargo administrativo de coordenação: cargo={}, setor={}", 
                    cargoEmprego, setorExercicio);
        
        // Critério 1: Cargo contém palavras-chave administrativas
        if (cargoEmprego != null && !cargoEmprego.trim().isEmpty()) {
            String cargoUpper = cargoEmprego.toUpperCase();
            
            for (String keyword : ADMINISTRATIVE_COORDINATOR_KEYWORDS) {
                if (cargoUpper.contains(keyword)) {
                    logger.debug("Cargo '{}' contém palavra-chave administrativa '{}'", 
                               cargoEmprego, keyword);
                    return true;
                }
            }
        }
        
        // Critério 2: Setor contém "COORD" (específico para cargos administrativos)
        if (setorExercicio != null && !setorExercicio.trim().isEmpty()) {
            String setorUpper = setorExercicio.toUpperCase();
            
            if (setorUpper.contains("COORD")) {
                logger.debug("Setor de exercício '{}' contém 'COORD', considerado coordenador administrativo", 
                           setorExercicio);
                return true;
            }
        }
        
        logger.debug("Servidor não possui cargo administrativo de coordenação");
        return false;
    }
    
    /**
     * Verifica se o cargo é de professor.
     * 
     * @param cargoEmprego Cargo/emprego do servidor
     * @return true se é professor
     */
    private boolean isProfessor(String cargoEmprego) {
        if (cargoEmprego == null || cargoEmprego.trim().isEmpty()) {
            logger.debug("Cargo é null ou vazio, não é professor");
            return false;
        }
        
        String cargoUpper = cargoEmprego.toUpperCase();
        
        for (String keyword : PROFESSOR_KEYWORDS) {
            if (cargoUpper.contains(keyword)) {
                logger.debug("Cargo '{}' contém palavra-chave de professor '{}'", cargoEmprego, keyword);
                return true;
            }
        }
        
        logger.debug("Cargo '{}' não contém palavras-chave de professor", cargoEmprego);
        return false;
    }
}