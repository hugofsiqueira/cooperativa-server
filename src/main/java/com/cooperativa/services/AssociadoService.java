package com.cooperativa.services;

import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.Associado;
import com.cooperativa.repositories.AssociadoRepository;
import org.springframework.stereotype.Service;

@Service
public class AssociadoService extends GenericServiceImpl<Associado, String, AssociadoRepository> {

    @Override
    public void validarEntidadeInserir(Associado entity) throws ApplicationException {
        if (entity == null) {
            throw new ApplicationException("Objeto não instanciado");
        }
        if (entity.getId() == null) {
            throw new ApplicationException("Id do Associado não informado");
        }
    }

    @Override
    public void validarEntidadeAtualizar(Associado entity) throws ApplicationException {
        if (entity == null) {
            throw new ApplicationException("Objeto não instanciado");
        }
        if (entity.getId() == null) {
            throw new ApplicationException("Id do Associado não informado");
        }
    }
}
