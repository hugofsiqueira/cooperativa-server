package com.cooperativa.dto;

import com.cooperativa.model.Pauta;

public class SessaoDTO {

    private String codigoPauta;
    private Long duracao;
    private Pauta pauta;

    public String getCodigoPauta() {
        return codigoPauta;
    }

    public void setCodigoPauta(String codigoPauta) {
        this.codigoPauta = codigoPauta;
    }

    public Long getDuracao() {
        return duracao;
    }

    public void setDuracao(Long duracao) {
        this.duracao = duracao;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }
}
