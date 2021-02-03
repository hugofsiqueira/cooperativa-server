package com.cooperativa.model;

public enum OpcaoVoto {

  SIM("Sim"),
  NAO("Não");

  private String label;

  private OpcaoVoto(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
