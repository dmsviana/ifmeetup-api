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
        
        String generatedEmail = generateEmailFromName(servidor.nome());
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
            null, // curso - não existe em servidor
            null  // situacaoAluno - não existe em servidor
        );
    }
    
    public static SuapUserData fromAluno(SuapAlunoResponse aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("SuapAlunoResponse não pode ser null");
        }
        
        String generatedEmail = generateEmailFromName(aluno.nome());
        String curso = aluno.curso() != null ? aluno.curso().nome() : null;
        
        return new SuapUserData(
            aluno.uuid(),
            aluno.nome(),
            aluno.matricula(),
            SuapUserType.ALUNO,
            generatedEmail,
            null, // cargoEmprego - não existe em aluno
            null, // funcaoCodigo - não existe em aluno
            null, // setorExercicio - não existe em aluno
            null, // situacao - não existe em aluno
            curso,
            aluno.situacao()
        );
    }
    
    private static String generateEmailFromName(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser null ou vazio");
        }
        
        // normaliza o nome removendo acentos e caracteres especiais
        String normalized = nome.trim()
            .toLowerCase()
            .replaceAll("[áàâãä]", "a")
            .replaceAll("[éèêë]", "e")
            .replaceAll("[íìîï]", "i")
            .replaceAll("[óòôõö]", "o")
            .replaceAll("[úùûü]", "u")
            .replaceAll("[ç]", "c")
            .replaceAll("[^a-z\\s]", "") // remove caracteres especiais
            .replaceAll("\\s+", " "); // normaliza espaços
        
        String[] parts = normalized.split("\\s+");
        
        if (parts.length == 1) {
            // nome com uma palavra: "diogo@ifpb.edu.br"
            return parts[0] + "@ifpb.edu.br";
        } else {
            // nome com múltiplas palavras: "diogo.marcelo@ifpb.edu.br"
            return parts[0] + "." + parts[parts.length - 1] + "@ifpb.edu.br";
        }
    }
    
    public boolean isServidor() {
        return userType == SuapUserType.SERVIDOR;
    }
    
    public boolean isAluno() {
        return userType == SuapUserType.ALUNO;
    }
}