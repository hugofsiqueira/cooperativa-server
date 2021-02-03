package com.cooperativa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class Pauta extends AbstractEntity<String>{

    @Id
    private String codigo;
    private Votacao votacao;
    private Date dataCriacao;

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

    public Date getDataCriacao() {
      return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
      this.dataCriacao = dataCriacao;
    }

  @Override
    public String getId() {
        return codigo;
    }
}
