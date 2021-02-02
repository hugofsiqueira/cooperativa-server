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
    private Long timeout;

    public Votacao() {
        votoList = new ArrayList<>();
    }

    @JsonIgnore
    private List<Voto> votoList;
    private boolean finalizada;

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

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
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
