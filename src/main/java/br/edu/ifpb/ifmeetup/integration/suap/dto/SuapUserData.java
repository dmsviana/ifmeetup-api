package br.edu.ifpb.ifmeetup.integration.suap.dto;

import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;

public record SuapUserData(
    String uuid,
    String nome,
    String matricula,
    SuapUserType userType,
    String generatedEmail,
    
    // Campos específicos de servidor
    String cargoEmprego,
    Integer funcaoCodigo,
    String setorExercicio,
    String situacao,
    
    // Campos específicos de aluno
    String curso,
    String situacaoAluno
) {
    
    public static SuapUserData fromServidor(SuapServidorResponse servidor) {
        if (servidor == null) {
            throw new IllegalArgumentException("SuapServidorResponse não pode ser null");
        }
        
        String generatedEmail = generateEmailFromMatricula(servidor.matricula());
        String setorExercicio = servidor.setorExercicio() != null ? servidor.setorExercicio().nome() : null;
        String situacao = servidor.situacao() != null ? servidor.situacao().nome() : null;
        
        return new SuapUserData(
            servidor.uuid(),
            servidor.nome(),
            servidor.matricula(),
            SuapUserType.SERVIDOR,
            generatedEmail,
            servidor.cargoEmprego(),
            servidor.funcaoCodigo(),
            setorExercicio,
            situacao,
            null, // curso - não aplicável para servidor
            null  // situacaoAluno - não aplicável para servidor
        );
    }
    
    public static SuapUserData fromAluno(SuapAlunoResponse aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("SuapAlunoResponse não pode ser null");
        }
        
        String generatedEmail = generateEmailFromMatricula(aluno.matricula());
        String curso = aluno.curso() != null ? aluno.curso().nome() : null;
        
        return new SuapUserData(
            aluno.uuid(),
            aluno.nome(),
            aluno.matricula(),
            SuapUserType.ALUNO,
            generatedEmail,
            null, // cargoEmprego - não aplicável para aluno
            null, // funcaoCodigo - não aplicável para aluno
            null, // setorExercicio - não aplicável para aluno
            null, // situacao - não aplicável para aluno
            curso,
            aluno.situacao()
        );
    }
    
    private static String generateEmailFromMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("Matrícula não pode ser null ou vazia");
        }
        
        return matricula.trim() + "@ifpb.edu.br";
    }
    
    public boolean isServidor() {
        return userType == SuapUserType.SERVIDOR;
    }
    
    public boolean isAluno() {
        return userType == SuapUserType.ALUNO;
    }
}