package br.com.catolicapb.facerecoptm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccessRecord {
    private int id;
    private int pessoaId;
    private String nome;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
}
