package br.com.catolicapb.facerecoptm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

@Data
@AllArgsConstructor
public class Pessoa {

    private Integer id;
    private String nome, cpf, turma;
    private Date registerDate;
    private Boolean isActive;

    public Pessoa(String nome, String cpf, String turma, Boolean isActive) {
        this.nome = nome;
        this.cpf = cpf;
        this.turma = turma;
        this.isActive = isActive;
    }

    public Pessoa(Integer id, String nome, String cpf, String turma, Boolean isActive) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.turma = turma;
        this.isActive = isActive;
    }
}
