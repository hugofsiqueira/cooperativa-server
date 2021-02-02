package com.cooperativa.controllers;

import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.Associado;
import com.cooperativa.services.AssociadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/associados")
public class AssociadoController {

    @Autowired
    private AssociadoService associadoService;

    @PostMapping(value = "/v1/inserir")
    public Mono<Associado> inserir(@RequestBody Associado associado) throws ApplicationException {
        return associadoService.inserir(associado);
    }
}
