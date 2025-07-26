package br.edu.ifpb.ifmeetup.domain.enums;


public enum SuapUserType {
    
    SERVIDOR("Servidor"),
    ALUNO("Aluno");
    
    private final String description;
    
    SuapUserType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}