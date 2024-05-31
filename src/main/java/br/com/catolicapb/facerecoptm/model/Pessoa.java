package br.com.catolicapb.facerecoptm.model;

import lombok.Data;

@Data
public class Pessoa {

    private Integer id;
    private String nome, cpf;

    public Pessoa(Integer id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }

    public Pessoa(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }
}
