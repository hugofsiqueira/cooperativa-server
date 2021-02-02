package com.cooperativa.services;

import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.AbstractEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public abstract class GenericServiceImpl<E extends AbstractEntity, K extends Serializable, R extends ReactiveMongoRepository<E,K>>
        implements GenericService<E, K> {

    @Autowired
    private R repository;

    @Override
    public Mono<E> inserir(E entity) throws ApplicationException {
        validarEntidadeInserir(entity);
        Mono<E> busca = procurarPorId((K) entity.getId());
        return busca.defaultIfEmpty(entity)
                .filter(e -> e == entity || !e.getId().equals(entity.getId()))
                .switchIfEmpty(Mono.error(new ApplicationException("Entidade com o mesmo id já inserida")))
                .flatMap(repository::save);
    }

    private void validarId(K id) throws ApplicationException {
        if (id == null) {
            throw new ApplicationException("Id deve ser informado para a busca");
        }
    }

    @Override
    public Mono<E> procurarPorId(K id) throws ApplicationException {
        validarId(id);
        return repository.findById(id);
    }

    @Override
    public Mono<Void> remover(K id) throws ApplicationException {
        validarId(id);
        return procurarPorId(id)
                .map(e -> id)
                .flatMap(repository::deleteById)
                .switchIfEmpty(Mono.error(new ApplicationException("Entidade não encontrada")));
    }

    @Override
    public Mono<E> atualizar(E entity) throws ApplicationException {
        validarEntidadeAtualizar(entity);
        return procurarPorId((K) entity.getId())
                .switchIfEmpty(Mono.error(new ApplicationException("Entidade não encontrada")))
                .map(e -> entity)
                .flatMap(repository::save);
    }

    public R getRepository() {
        return repository;
    }

    public void setRepository(R repository) {
        this.repository = repository;
    }

    public abstract void validarEntidadeInserir(E entity) throws ApplicationException;
    public abstract void validarEntidadeAtualizar(E entity) throws ApplicationException;
}
