package com.cooperativa.services;

import com.cooperativa.model.Pauta;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ResultadoPautaSender {

  @Autowired
  private AmqpTemplate rabbitTemplate;

  static final String EXCHANGE_NAME = "pautas.exchange";
  static final String ROUTE_KEY = "pautas.route";


  public Mono<Pauta> enviarResultado(Pauta pauta) {
    Mono<Pauta> pautaMono = Mono.just(pauta);
    return pautaMono.flatMap( p -> {
      rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTE_KEY, p);
      System.out.println("Enviando resultado da Pauta de código "+p.getCodigo());
      return pautaMono;
    });
  }

}
