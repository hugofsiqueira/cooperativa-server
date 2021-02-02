package com.cooperativa.controllers;

import com.cooperativa.dto.SessaoDTO;
import com.cooperativa.dto.VotoDTO;
import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.Pauta;
import com.cooperativa.model.Votacao;
import com.cooperativa.model.Voto;
import com.cooperativa.services.PautaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pautas")
public class PautaController {

    @Autowired
    private PautaService pautaService;

    @PostMapping(value = "/v1/inserir")
    public Mono<Pauta> inserir(@RequestBody Pauta pauta) throws ApplicationException {
        return pautaService.inserir(pauta);
    }

    @PostMapping(value = "/v1/abrir-votacao")
    public Mono<Pauta> abrirVotacao(@RequestBody SessaoDTO sessaoDTO) throws ApplicationException {
        return pautaService.abrirVotacao(sessaoDTO);
    }

    @PostMapping(value = "/v1/registrar-voto")
    public Mono<Voto> registrarVoto(@RequestBody VotoDTO votoDTO) throws ApplicationException {
        return pautaService.registrarVoto(votoDTO);
    }
}
