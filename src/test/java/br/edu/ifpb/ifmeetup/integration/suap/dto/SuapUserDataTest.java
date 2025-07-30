package br.edu.ifpb.ifmeetup.integration.suap.dto;

import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuapUserDataTest {

    @Test
    void shouldCreateSuapUserDataFromServidor() {
        // Given
        SuapSetorResponse setor = new SuapSetorResponse("uuid-setor", "COORD-INFO", "Coordenação de Informática");
        SuapSituacaoResponse situacao = new SuapSituacaoResponse("ATIVO", "Ativo Permanente");
        SuapServidorResponse servidor = new SuapServidorResponse(
            "uuid-felipe",
            "Felipe Omena",
            "1323726",
            "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
            1,
            setor,
            situacao
        );

        // When
        SuapUserData userData = SuapUserData.fromServidor(servidor);

        // Then
        assertEquals("uuid-felipe", userData.uuid());
        assertEquals("Felipe Omena", userData.nome());
        assertEquals("1323726", userData.matricula());
        assertEquals(SuapUserType.SERVIDOR, userData.userType());
        assertEquals("felipe.omena@ifpb.edu.br", userData.generatedEmail());
        assertEquals("PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO", userData.cargoEmprego());
        assertEquals(1, userData.funcaoCodigo());
        assertEquals("Coordenação de Informática", userData.setorExercicio());
        assertEquals("Ativo Permanente", userData.situacao());
        assertNull(userData.curso());
        assertNull(userData.situacaoAluno());
        assertTrue(userData.isServidor());
        assertFalse(userData.isAluno());
    }

    @Test
    void shouldCreateSuapUserDataFromServidorWithNullSetor() {
        // Given
        SuapSituacaoResponse situacao = new SuapSituacaoResponse("ATIVO", "Ativo Permanente");
        SuapServidorResponse servidor = new SuapServidorResponse(
            "uuid-damiao",
            "Damião Ribeiro",
            "3360670",
            "PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO",
            null,
            null, // setor null
            situacao
        );

        // When
        SuapUserData userData = SuapUserData.fromServidor(servidor);

        // Then
        assertEquals("uuid-damiao", userData.uuid());
        assertEquals("Damião Ribeiro", userData.nome());
        assertEquals("3360670", userData.matricula());
        assertEquals(SuapUserType.SERVIDOR, userData.userType());
        assertEquals("damiao.ribeiro@ifpb.edu.br", userData.generatedEmail());
        assertEquals("PROFESSOR DO ENSINO BÁSICO, TÉCNICO E TECNOLÓGICO", userData.cargoEmprego());
        assertNull(userData.funcaoCodigo());
        assertNull(userData.setorExercicio());
        assertEquals("Ativo Permanente", userData.situacao());
        assertNull(userData.curso());
        assertNull(userData.situacaoAluno());
        assertTrue(userData.isServidor());
        assertFalse(userData.isAluno());
    }

    @Test
    void shouldCreateSuapUserDataFromAluno() {
        // Given
        SuapCursoResponse curso = new SuapCursoResponse("uuid-curso", "Tecnologia em Sistemas para Internet");
        SuapAlunoResponse aluno = new SuapAlunoResponse(
            "uuid-diogo",
            "Diogo Marcelo",
            "202215020007",
            curso,
            "MATRICULADO"
        );

        // When
        SuapUserData userData = SuapUserData.fromAluno(aluno);

        // Then
        assertEquals("uuid-diogo", userData.uuid());
        assertEquals("Diogo Marcelo", userData.nome());
        assertEquals("202215020007", userData.matricula());
        assertEquals(SuapUserType.ALUNO, userData.userType());
        assertEquals("diogo.marcelo@ifpb.edu.br", userData.generatedEmail());
        assertNull(userData.cargoEmprego());
        assertNull(userData.funcaoCodigo());
        assertNull(userData.setorExercicio());
        assertNull(userData.situacao());
        assertEquals("Tecnologia em Sistemas para Internet", userData.curso());
        assertEquals("MATRICULADO", userData.situacaoAluno());
        assertFalse(userData.isServidor());
        assertTrue(userData.isAluno());
    }

    @Test
    void shouldCreateSuapUserDataFromAlunoWithNullCurso() {
        // Given
        SuapAlunoResponse aluno = new SuapAlunoResponse(
            "uuid-aluno",
            "Aluno Teste",
            "202215020008",
            null, // curso null
            "MATRICULADO"
        );

        // When
        SuapUserData userData = SuapUserData.fromAluno(aluno);

        // Then
        assertEquals("uuid-aluno", userData.uuid());
        assertEquals("Aluno Teste", userData.nome());
        assertEquals("202215020008", userData.matricula());
        assertEquals(SuapUserType.ALUNO, userData.userType());
        assertEquals("aluno.teste@ifpb.edu.br", userData.generatedEmail());
        assertNull(userData.cargoEmprego());
        assertNull(userData.funcaoCodigo());
        assertNull(userData.setorExercicio());
        assertNull(userData.situacao());
        assertNull(userData.curso());
        assertEquals("MATRICULADO", userData.situacaoAluno());
        assertFalse(userData.isServidor());
        assertTrue(userData.isAluno());
    }

    @Test
    void shouldThrowExceptionWhenServidorIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SuapUserData.fromServidor(null)
        );
        assertEquals("SuapServidorResponse não pode ser null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAlunoIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SuapUserData.fromAluno(null)
        );
        assertEquals("SuapAlunoResponse não pode ser null", exception.getMessage());
    }

    @Test
    void shouldGenerateEmailCorrectly() {
        // Given
        SuapCursoResponse curso = new SuapCursoResponse("uuid-curso", "Curso Teste");
        SuapAlunoResponse aluno = new SuapAlunoResponse(
            "uuid-test",
            "Test User",
            "123456789",
            curso,
            "ATIVO"
        );

        // When
        SuapUserData userData = SuapUserData.fromAluno(aluno);

        // Then
        assertEquals("test.user@ifpb.edu.br", userData.generatedEmail());
    }

    @Test
    void shouldGenerateEmailWithSpecialCharacters() {
        // Given
        SuapCursoResponse curso = new SuapCursoResponse("uuid-curso", "Curso Teste");
        SuapAlunoResponse aluno = new SuapAlunoResponse(
            "uuid-test",
            "José da Silva",
            "123456789",
            curso,
            "ATIVO"
        );

        // When
        SuapUserData userData = SuapUserData.fromAluno(aluno);

        // Then
        assertEquals("jose.silva@ifpb.edu.br", userData.generatedEmail());
    }

    @Test
    void shouldGenerateEmailWithSingleName() {
        // Given
        SuapCursoResponse curso = new SuapCursoResponse("uuid-curso", "Curso Teste");
        SuapAlunoResponse aluno = new SuapAlunoResponse(
            "uuid-test",
            "Madonna",
            "123456789",
            curso,
            "ATIVO"
        );

        // When
        SuapUserData userData = SuapUserData.fromAluno(aluno);

        // Then
        assertEquals("madonna@ifpb.edu.br", userData.generatedEmail());
    }

    @Test
    void shouldGenerateEmailWithAccentsAndSpecialChars() {
        // Given
        SuapSetorResponse setor = new SuapSetorResponse("uuid-setor", "DEPTO", "Departamento");
        SuapSituacaoResponse situacao = new SuapSituacaoResponse("ATIVO", "Ativo");
        SuapServidorResponse servidor = new SuapServidorResponse(
            "uuid-test",
            "João Ção Ñoño",
            "1234567",
            "PROFESSOR",
            null,
            setor,
            situacao
        );

        // When
        SuapUserData userData = SuapUserData.fromServidor(servidor);

        // Then
        assertEquals("joao.nono@ifpb.edu.br", userData.generatedEmail());
    }
}