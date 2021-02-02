package com.cooperativa.dto;

import com.cooperativa.model.Pauta;

public class SessaoDTO {

    private String codigoPauta;
    private Long duracaoMinutos;
    private Pauta pauta;

    public String getCodigoPauta() {
        return codigoPauta;
    }

    public void setCodigoPauta(String codigoPauta) {
        this.codigoPauta = codigoPauta;
    }

    public Long getDuracaoMinutos() {
      return duracaoMinutos;
    }

    public void setDuracaoMinutos(Long duracaoMinutos) {
      this.duracaoMinutos = duracaoMinutos;
    }

  public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }
}
