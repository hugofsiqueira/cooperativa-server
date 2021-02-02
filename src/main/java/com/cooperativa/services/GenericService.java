package com.cooperativa.services;

import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.AbstractEntity;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public interface GenericService<E extends AbstractEntity, K extends Serializable> {

    Mono<E> inserir(E entity) throws ApplicationException;
    Mono<E> procurarPorId(K id) throws ApplicationException;
    Mono<Void> remover(K id) throws ApplicationException;
    Mono<E> atualizar(E entity) throws ApplicationException;

}
