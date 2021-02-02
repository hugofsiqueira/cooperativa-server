package com.cooperativa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public abstract class AbstractEntity<K extends Serializable> {

    @JsonIgnore
    public abstract K getId();

}
