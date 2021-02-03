package com.cooperativa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Pauta extends AbstractEntity<String>{

    @Id
    private String codigo;
    private Votacao votacao;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Votacao getVotacao() {
      return votacao;
    }

    public void setVotacao(Votacao votacao) {
      this.votacao = votacao;
    }

    public boolean temVotacaoEmAndamento() {
      return votacao != null && !votacao.isFinalizada();
    }

    public boolean temVotacaoFinalizada() {
      return votacao != null && votacao.isFinalizada();
    }

    @Override
    public String getId() {
        return codigo;
    }
}
