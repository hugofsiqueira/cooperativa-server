package com.cooperativa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
public class Votacao extends AbstractEntity<ObjectId> implements Comparable<Votacao>{

    @Id
    private ObjectId id;
    private Date dataInicio;
    private Long duracaoMinutos;
    private Resultado resultado;

    public Votacao() {
        votoList = new ArrayList<>();
    }

    @JsonIgnore
    private List<Voto> votoList;
    private boolean finalizada;

    public void apurarResultado() {
      if (votoList != null && !votoList.isEmpty()) {
        Long totalVotos = (long)votoList.size();
        Long votosSim = votoList.stream().filter(voto -> voto.getOpcao().equals(OpcaoVoto.SIM.getLabel())).count();
        Long votosNao = totalVotos - votosSim;
        Resultado resultado = new Resultado();
        resultado.setTotalSim(votosSim);
        resultado.setTotalNao(votosNao);
        resultado.setTotalVotos(totalVotos);
        setResultado(resultado);
      }
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public List<Voto> getVotoList() {
        return votoList;
    }

    public void setVotoList(List<Voto> votoList) {
        this.votoList = votoList;
    }

    public Long getDuracaoMinutos() {
      return duracaoMinutos;
    }

    public void setDuracaoMinutos(Long duracaoMinutos) {
      this.duracaoMinutos = duracaoMinutos;
    }

    public Resultado getResultado() {
      return resultado;
    }

    public void setResultado(Resultado resultado) {
      this.resultado = resultado;
    }

  @Override
    public ObjectId getId() {
        return id;
    }

    @Override
    public int compareTo(Votacao o) {
        return this.dataInicio.compareTo(o.getDataInicio());
    }
}
