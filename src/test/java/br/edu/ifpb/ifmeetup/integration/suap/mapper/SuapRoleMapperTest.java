package br.edu.ifpb.ifmeetup.integration.suap.mapper;

import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapUserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SuapRoleMapper Tests")
class SuapRoleMapperTest {
    
    private SuapRoleMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new SuapRoleMapper();
    }
    
    @Nested
    @DisplayName("Student Profile Mapping Tests")
    class StudentProfileTests {
        
        @Test
        @DisplayName("Should map ALUNO to ProfileType.STUDENT")
        void shouldMapAlunoToStudent() {
            // Arrange
            SuapUserData alunoData = new SuapUserData(
                "uuid-aluno", "Diogo Marcelo", "202215020007", SuapUserType.ALUNO,
                "202215020007@ifpb.edu.br", null, null, null, null,
                "Tecnologia em Sistemas para Internet", "MATRICULADO"
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(alunoData);
            
            // Assert
            assertEquals(ProfileType.STUDENT, result);
        }
        
        @Test
        @DisplayName("Should map ALUNO to STUDENT even with incomplete course data")
        void shouldMapAlunoToStudentWithIncompleteData() {
            // Arrange - Aluno com dados incompletos
            SuapUserData alunoData = new SuapUserData(
                "uuid-aluno", "João Silva", "202215020008", SuapUserType.ALUNO,
                "202215020008@ifpb.edu.br", null, null, null, null,
                null, null // curso e situação nulos
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(alunoData);
            
            // Assert
            assertEquals(ProfileType.STUDENT, result);
        }
        
        @Test
        @DisplayName("Should map ALUNO to STUDENT with empty course data")
        void shouldMapAlunoToStudentWithEmptyCourseData() {
            // Arrange - Aluno com strings vazias
            SuapUserData alunoData = new SuapUserData(
                "uuid-aluno", "Maria Santos", "202215020009", SuapUserType.ALUNO,
                "202215020009@ifpb.edu.br", null, null, null, null,
                "", "" // curso e situação vazios
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(alunoData);
            
            // Assert
            assertEquals(ProfileType.STUDENT, result);
        }
    }
    
    @Nested
    @DisplayName("Coordinator Profile Mapping Tests")
    class CoordinatorProfileTests {
        
        @Test
        @DisplayName("Should map professor with funcaoCodigo to COORDINATOR")
        void shouldMapProfessorWithFuncaoToCoordinator() {
            // Arrange - Professor com função de coordenação (funcaoCodigo != null)
            SuapUserData professorData = new SuapUserData(
                "uuid-felipe", "Felipe Omena", "1323726", SuapUserType.SERVIDOR,
                "1323726@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                1, "COORD-INFO", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map professor with coordination sector to COORDINATOR")
        void shouldMapProfessorWithCoordinationSectorToCoordinator() {
            // Arrange - Professor com setor de coordenação
            SuapUserData professorData = new SuapUserData(
                "uuid-coord", "Ana Silva", "1234567", SuapUserType.SERVIDOR,
                "1234567@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "COORD-EXTENSÃO", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map professor with DIRETOR sector to COORDINATOR")
        void shouldMapProfessorWithDiretorSectorToCoordinator() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-diretor", "Carlos Santos", "2345678", SuapUserType.SERVIDOR,
                "2345678@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "DIRETOR-GERAL", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map professor with GESTÃO sector to COORDINATOR")
        void shouldMapProfessorWithGestaoSectorToCoordinator() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-gestao", "Lucia Oliveira", "3456789", SuapUserType.SERVIDOR,
                "3456789@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "GESTÃO-PESSOAS", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map professor with CHEFIA sector to COORDINATOR")
        void shouldMapProfessorWithChefiaSectorToCoordinator() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-chefia", "Roberto Lima", "4567890", SuapUserType.SERVIDOR,
                "4567890@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "CHEFIA-GABINETE", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map administrative DIRETOR to COORDINATOR")
        void shouldMapAdministrativeDiretorToCoordinator() {
            // Arrange
            SuapUserData diretorData = new SuapUserData(
                "uuid-diretor", "João Silva", "1234567", SuapUserType.SERVIDOR,
                "1234567@ifpb.edu.br", "DIRETOR GERAL", null, "DIREÇÃO GERAL", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(diretorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map administrative COORDENADOR to COORDINATOR")
        void shouldMapAdministrativeCoordenadorToCoordinator() {
            // Arrange
            SuapUserData coordData = new SuapUserData(
                "uuid-coord", "Maria Santos", "2345678", SuapUserType.SERVIDOR,
                "2345678@ifpb.edu.br", "COORDENADOR DE CURSO", null, "COORD-TSI", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(coordData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map administrative GERENTE to COORDINATOR")
        void shouldMapAdministrativeGerenteToCoordinator() {
            // Arrange
            SuapUserData gerenteData = new SuapUserData(
                "uuid-gerente", "Pedro Alves", "3456789", SuapUserType.SERVIDOR,
                "3456789@ifpb.edu.br", "GERENTE DE TI", null, "SETOR-TI", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(gerenteData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map administrative CHEFE to COORDINATOR")
        void shouldMapAdministrativeChefeToCoordinator() {
            // Arrange
            SuapUserData chefeData = new SuapUserData(
                "uuid-chefe", "Ana Costa", "4567890", SuapUserType.SERVIDOR,
                "4567890@ifpb.edu.br", "CHEFE DE DEPARTAMENTO", null, "DEPTO-ADMIN", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(chefeData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
        
        @Test
        @DisplayName("Should map non-professor with COORD sector to COORDINATOR")
        void shouldMapNonProfessorWithCoordSectorToCoordinator() {
            // Arrange - Técnico administrativo em setor de coordenação
            SuapUserData tecnicoData = new SuapUserData(
                "uuid-tecnico", "Carlos Silva", "5678901", SuapUserType.SERVIDOR,
                "5678901@ifpb.edu.br", "TÉCNICO ADMINISTRATIVO", null, "COORD-INFO", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(tecnicoData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
    }
    
    @Nested
    @DisplayName("Teacher Profile Mapping Tests")
    class TeacherProfileTests {
        
        @Test
        @DisplayName("Should map professor without coordination function to TEACHER")
        void shouldMapProfessorWithoutCoordinationToTeacher() {
            // Arrange - Professor sem função de coordenação
            SuapUserData professorData = new SuapUserData(
                "uuid-damiao", "Damião Ribeiro", "3360670", SuapUserType.SERVIDOR,
                "3360670@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "DEPTO-INFO", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map professor with regular department to TEACHER")
        void shouldMapProfessorWithRegularDepartmentToTeacher() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-prof", "José Santos", "6789012", SuapUserType.SERVIDOR,
                "6789012@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "DEPARTAMENTO DE MATEMÁTICA", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map non-professor servidor to TEACHER as default")
        void shouldMapNonProfessorServidorToTeacherAsDefault() {
            // Arrange - Servidor que não é professor nem cargo administrativo de coordenação
            SuapUserData servidorData = new SuapUserData(
                "uuid-servidor", "Maria Oliveira", "7890123", SuapUserType.SERVIDOR,
                "7890123@ifpb.edu.br", "TÉCNICO ADMINISTRATIVO", null, "SETOR-FINANCEIRO", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(servidorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map servidor with unknown cargo to TEACHER as default")
        void shouldMapServidorWithUnknownCargoToTeacherAsDefault() {
            // Arrange
            SuapUserData servidorData = new SuapUserData(
                "uuid-servidor", "Paulo Silva", "8901234", SuapUserType.SERVIDOR,
                "8901234@ifpb.edu.br", "CARGO DESCONHECIDO", null, "SETOR-QUALQUER", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(servidorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Incomplete Data Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should return TEACHER as default when userData is null")
        void shouldReturnTeacherDefaultWhenUserDataIsNull() {
            // Act
            ProfileType result = mapper.mapToProfileType(null);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should return TEACHER as default when userType is null")
        void shouldReturnTeacherDefaultWhenUserTypeIsNull() {
            // Arrange
            SuapUserData userData = new SuapUserData(
                "uuid-test", "Test User", "1234567", null,
                "1234567@ifpb.edu.br", null, null, null, null, null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(userData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map servidor with null cargoEmprego to TEACHER")
        void shouldMapServidorWithNullCargoEmpregoToTeacher() {
            // Arrange
            SuapUserData servidorData = new SuapUserData(
                "uuid-servidor", "Ana Silva", "9012345", SuapUserType.SERVIDOR,
                "9012345@ifpb.edu.br", null, null, "SETOR-QUALQUER", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(servidorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map servidor with empty cargoEmprego to TEACHER")
        void shouldMapServidorWithEmptyCargoEmpregoToTeacher() {
            // Arrange
            SuapUserData servidorData = new SuapUserData(
                "uuid-servidor", "Carlos Lima", "0123456", SuapUserType.SERVIDOR,
                "0123456@ifpb.edu.br", "", null, "SETOR-QUALQUER", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(servidorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map servidor with whitespace-only cargoEmprego to TEACHER")
        void shouldMapServidorWithWhitespaceCargoEmpregoToTeacher() {
            // Arrange
            SuapUserData servidorData = new SuapUserData(
                "uuid-servidor", "Roberto Santos", "1234560", SuapUserType.SERVIDOR,
                "1234560@ifpb.edu.br", "   ", null, "SETOR-QUALQUER", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(servidorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map professor with null setorExercicio and null funcaoCodigo to TEACHER")
        void shouldMapProfessorWithNullDataToTeacher() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-prof", "Lucia Costa", "2345601", SuapUserType.SERVIDOR,
                "2345601@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, null, "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should map professor with empty setorExercicio and null funcaoCodigo to TEACHER")
        void shouldMapProfessorWithEmptySetorToTeacher() {
            // Arrange
            SuapUserData professorData = new SuapUserData(
                "uuid-prof", "Fernando Alves", "3456012", SuapUserType.SERVIDOR,
                "3456012@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should handle case-insensitive cargo matching")
        void shouldHandleCaseInsensitiveCargoMatching() {
            // Arrange - Cargo em minúsculas
            SuapUserData professorData = new SuapUserData(
                "uuid-prof", "Marina Silva", "4567123", SuapUserType.SERVIDOR,
                "4567123@ifpb.edu.br", "professor do ensino básico, técnico e tecnológico",
                null, "depto-info", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.TEACHER, result);
        }
        
        @Test
        @DisplayName("Should handle case-insensitive setor matching for coordination")
        void shouldHandleCaseInsensitiveSetorMatching() {
            // Arrange - Setor em minúsculas
            SuapUserData professorData = new SuapUserData(
                "uuid-prof", "Gabriel Santos", "5678234", SuapUserType.SERVIDOR,
                "5678234@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "coord-info", "ATIVO", null, null
            );
            
            // Act
            ProfileType result = mapper.mapToProfileType(professorData);
            
            // Assert
            assertEquals(ProfileType.COORDINATOR, result);
        }
    }
    
    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {
        
        @Test
        @DisplayName("isCoordinator should return true with funcaoCodigo")
        void isCoordinatorShouldReturnTrueWithFuncaoCodigo() {
            // Act & Assert
            assertTrue(mapper.isCoordinator(1, null));
            assertTrue(mapper.isCoordinator(123, "DEPTO-INFO"));
            assertTrue(mapper.isCoordinator(0, null)); // Zero também é válido
        }
        
        @Test
        @DisplayName("isCoordinator should return true with coordination sector keywords")
        void isCoordinatorShouldReturnTrueWithCoordinationSectorKeywords() {
            // Act & Assert
            assertTrue(mapper.isCoordinator(null, "COORD-INFO"));
            assertTrue(mapper.isCoordinator(null, "DIRETOR-GERAL"));
            assertTrue(mapper.isCoordinator(null, "GESTÃO-PESSOAS"));
            assertTrue(mapper.isCoordinator(null, "CHEFIA-GABINETE"));
        }
        
        @Test
        @DisplayName("isCoordinator should return false without function or coordination sector")
        void isCoordinatorShouldReturnFalseWithoutFunctionOrCoordinationSector() {
            // Act & Assert
            assertFalse(mapper.isCoordinator(null, null));
            assertFalse(mapper.isCoordinator(null, "DEPTO-INFO"));
            assertFalse(mapper.isCoordinator(null, ""));
            assertFalse(mapper.isCoordinator(null, "   "));
        }
        
        @Test
        @DisplayName("isAdministrativeCoordinator should return true with administrative cargo keywords")
        void isAdministrativeCoordinatorShouldReturnTrueWithAdministrativeCargoKeywords() {
            // Act & Assert
            assertTrue(mapper.isAdministrativeCoordinator("DIRETOR GERAL", null));
            assertTrue(mapper.isAdministrativeCoordinator("COORDENADOR DE CURSO", null));
            assertTrue(mapper.isAdministrativeCoordinator("GERENTE DE TI", null));
            assertTrue(mapper.isAdministrativeCoordinator("CHEFE DE DEPARTAMENTO", null));
        }
        
        @Test
        @DisplayName("isAdministrativeCoordinator should return true with COORD sector")
        void isAdministrativeCoordinatorShouldReturnTrueWithCoordSector() {
            // Act & Assert
            assertTrue(mapper.isAdministrativeCoordinator("TÉCNICO ADMINISTRATIVO", "COORD-INFO"));
            assertTrue(mapper.isAdministrativeCoordinator(null, "COORD-EXTENSÃO"));
            assertTrue(mapper.isAdministrativeCoordinator("", "COORD-PESQUISA"));
        }
        
        @Test
        @DisplayName("isAdministrativeCoordinator should return false without administrative cargo or COORD sector")
        void isAdministrativeCoordinatorShouldReturnFalseWithoutAdministrativeCargoOrCoordSector() {
            // Act & Assert
            assertFalse(mapper.isAdministrativeCoordinator("TÉCNICO ADMINISTRATIVO", "DEPTO-INFO"));
            assertFalse(mapper.isAdministrativeCoordinator("PROFESSOR", "DEPTO-INFO"));
            assertFalse(mapper.isAdministrativeCoordinator(null, null));
            assertFalse(mapper.isAdministrativeCoordinator("", ""));
            assertFalse(mapper.isAdministrativeCoordinator("   ", "   "));
        }
    }
    
    @Nested
    @DisplayName("Real Examples from Design Document")
    class RealExamplesTests {
        
        @Test
        @DisplayName("Should correctly map real examples from design document")
        void shouldCorrectlyMapRealExamplesFromDesignDocument() {
            // Coordenador - Felipe Omena (baseado no design document)
            SuapUserData coordenador = new SuapUserData(
                "uuid-felipe", "Felipe Omena", "1323726", SuapUserType.SERVIDOR,
                "1323726@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                1, "Coordenação de Informática", "ATIVO", null, null
            );
            assertEquals(ProfileType.COORDINATOR, mapper.mapToProfileType(coordenador));
            
            // Professor - Damião Ribeiro (baseado no design document)
            SuapUserData professor = new SuapUserData(
                "uuid-damiao", "Damião Ribeiro", "3360670", SuapUserType.SERVIDOR,
                "3360670@ifpb.edu.br", "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
                null, "Departamento de Informática", "ATIVO", null, null
            );
            assertEquals(ProfileType.TEACHER, mapper.mapToProfileType(professor));
            
            // Aluno - Diogo Marcelo (baseado no design document)
            SuapUserData aluno = new SuapUserData(
                "uuid-diogo", "Diogo Marcelo", "202215020007", SuapUserType.ALUNO,
                "202215020007@ifpb.edu.br", null, null, null, null,
                "Tecnologia em Sistemas para Internet", "MATRICULADO"
            );
            assertEquals(ProfileType.STUDENT, mapper.mapToProfileType(aluno));
        }
    }
}