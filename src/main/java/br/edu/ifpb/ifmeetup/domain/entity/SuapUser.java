package br.edu.ifpb.ifmeetup.domain.entity;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "suap_users", indexes = {
    @Index(name = "idx_suap_user_matricula", columnList = "matricula", unique = true),
    @Index(name = "idx_suap_user_generated_email", columnList = "generated_email"),
    @Index(name = "idx_suap_user_suap_uuid", columnList = "suap_uuid")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuapUser extends BaseEntity {

    @NotBlank
    @Size(min = 7, max = 12)
    @Column(name = "matricula", nullable = false, unique = true)
    private String matricula;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private SuapUserType userType;

    @Size(max = 200)
    @Column(name = "cargo_emprego")
    private String cargoEmprego;

    @Column(name = "funcao_codigo")
    private Integer funcaoCodigo;

    @Size(max = 200)
    @Column(name = "setor_exercicio")
    private String setorExercicio;

    @Size(max = 50)
    @Column(name = "situacao")
    private String situacao;

    @Size(max = 200)
    @Column(name = "curso")
    private String curso;

    @Size(max = 50)
    @Column(name = "situacao_aluno")
    private String situacaoAluno;

    @NotBlank
    @Size(max = 100)
    @Column(name = "suap_uuid", nullable = false)
    private String suapUuid;

    @NotBlank
    @Size(max = 100)
    @Column(name = "generated_email", nullable = false)
    private String generatedEmail;

    public SuapUser(String matricula, String nome, SuapUserType userType, String suapUuid, String generatedEmail) {
        this.matricula = matricula;
        this.nome = nome;
        this.userType = userType;
        this.suapUuid = suapUuid;
        this.generatedEmail = generatedEmail;
    }

    public boolean isServidor() {
        return SuapUserType.SERVIDOR.equals(this.userType);
    }

    public boolean isAluno() {
        return SuapUserType.ALUNO.equals(this.userType);
    }
}