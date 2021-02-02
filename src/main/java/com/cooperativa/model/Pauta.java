package com.cooperativa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document
public class Pauta extends AbstractEntity<String>{

    @Id
    private String codigo;

    @JsonIgnore
    private List<Votacao> votacaoList;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public List<Votacao> getVotacaoList() {
        return votacaoList;
    }

    public void setVotacaoList(List<Votacao> votacaoList) {
        this.votacaoList = votacaoList;
    }

    public boolean temVotacaoEmAndamento() {
        if (votacaoList == null) {
            votacaoList = new ArrayList<>();
        }
        return votacaoList.stream().filter(v -> !v.isFinalizada()).count() > 0;
    }

    public Votacao buscarVotacaoAberta() {
        return votacaoList.stream().filter(v -> !v.isFinalizada()).findFirst().orElse(null);
    }

    public Votacao buscarUltimaVotacao() {
        if (!votacaoList.isEmpty()) {
            Collections.sort(votacaoList);
            Collections.reverse(votacaoList);
            return votacaoList.get(0);
        }
        return null;
    }

    @Override
    public String getId() {
        return codigo;
    }
}
