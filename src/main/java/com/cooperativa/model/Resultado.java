package com.cooperativa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Resultado extends AbstractEntity<String>{

    @Id
    private String codigo;
    private Long totalVotos;
    private Long totalSim;
    private Long totalNao;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getTotalVotos() {
      return totalVotos;
    }

    public void setTotalVotos(Long totalVotos) {
      this.totalVotos = totalVotos;
    }

    public Long getTotalSim() {
      return totalSim;
    }

    public void setTotalSim(Long totalSim) {
      this.totalSim = totalSim;
    }

    public Long getTotalNao() {
      return totalNao;
    }

    public void setTotalNao(Long totalNao) {
      this.totalNao = totalNao;
    }

  @Override
    public String getId() {
        return codigo;
    }
}
