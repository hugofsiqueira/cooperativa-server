package com.cooperativa.repositories;

import com.cooperativa.model.Associado;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AssociadoRepository extends ReactiveMongoRepository<Associado, String> {
}
