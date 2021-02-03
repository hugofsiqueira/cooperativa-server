package com.cooperativa.controllers;

import com.cooperativa.dto.SessaoDTO;
import com.cooperativa.dto.VotoDTO;
import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.Pauta;
import com.cooperativa.model.Votacao;
import com.cooperativa.model.Voto;
import com.cooperativa.services.PautaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pautas")
public class PautaController {

    @Autowired
    private PautaService pautaService;

    @GetMapping(value = "/v1/listar")
    public Flux<Pauta> listar() {
      return pautaService.listar();
    }

    @PostMapping(value = "/v1/inserir")
    public Mono<Pauta> inserir(@RequestBody Pauta pauta) {
      try {
        return pautaService.inserir(pauta);
      } catch (ApplicationException e) {
        return Mono.error(e);
      }
    }

    @PostMapping(value = "/v1/abrir-votacao")
    public Mono<Pauta> abrirVotacao(@RequestBody SessaoDTO sessaoDTO) {
      try {
        return pautaService.abrirVotacao(sessaoDTO);
      } catch (ApplicationException e) {
        return Mono.error(e);
      }
    }

    @PostMapping(value = "/v1/registrar-voto")
    public Mono<Voto> registrarVoto(@RequestBody VotoDTO votoDTO) {
      try {
        return pautaService.registrarVoto(votoDTO);
      } catch (ApplicationException e) {
        return Mono.error(e);
      }
    }
}
