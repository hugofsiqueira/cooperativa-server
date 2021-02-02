package com.cooperativa.repositories;

import com.cooperativa.model.Pauta;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PautaRepository extends ReactiveMongoRepository<Pauta, String> {
}
