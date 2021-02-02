package com.cooperativa.dto;

import com.cooperativa.model.Associado;
import com.cooperativa.model.Pauta;

public class VotoDTO {

    private String cpf;
    private String codigoPauta;
    private String opcao;

    private Associado associado;
    private Pauta pauta;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCodigoPauta() {
        return codigoPauta;
    }

    public void setCodigoPauta(String codigoPauta) {
        this.codigoPauta = codigoPauta;
    }

    public String getOpcao() {
        return opcao;
    }

    public void setOpcao(String opcao) {
        this.opcao = opcao;
    }

    public Associado getAssociado() {
        return associado;
    }

    public void setAssociado(Associado associado) {
        this.associado = associado;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }
}
