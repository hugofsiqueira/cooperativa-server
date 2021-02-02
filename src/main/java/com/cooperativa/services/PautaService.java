package com.cooperativa.services;

import com.cooperativa.dto.SessaoDTO;
import com.cooperativa.dto.StatusCPFDTO;
import com.cooperativa.dto.VotoDTO;
import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.Associado;
import com.cooperativa.model.Pauta;
import com.cooperativa.model.Votacao;
import com.cooperativa.model.Voto;
import com.cooperativa.repositories.PautaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

@Service
public class PautaService extends GenericServiceImpl<Pauta, String, PautaRepository> {

    @Autowired
    private AssociadoService associadoService;
    private WebClient webClientCPF;

    @PostConstruct
    public void registrarWebClient() {
        webClientCPF = WebClient.create("https://user-info.herokuapp.com");
    }

    public Mono<Voto> registrarVoto(VotoDTO votoDTO) throws ApplicationException {
        validarVotoDTO(votoDTO);
        return procurarPorId(votoDTO.getCodigoPauta())
                .switchIfEmpty(Mono.error(new ApplicationException("Pauta não encontrada")))
                .map(p -> {
                    votoDTO.setPauta(p);
                    return votoDTO;
                })
                .flatMap(this::validarPautaParaVoto)
                .flatMap(this::validarAssociadoParaVoto)
                .flatMap(this::salvarVotoEAtualizarPauta);
    }

    private Mono<Voto> salvarVotoEAtualizarPauta(VotoDTO votoDTO) {
        Pauta pauta = votoDTO.getPauta();
        Votacao votacao = pauta.buscarVotacaoAberta();

        Voto voto = new Voto();
        voto.setAssociado(votoDTO.getAssociado());
        voto.setOpcao(votoDTO.getOpcao());

        votacao.getVotoList().add(voto);
        return getRepository().save(pauta)
                .map(p -> voto)
                .flatMap(Mono::just);
    }

    private Mono<VotoDTO> validarAssociadoParaVoto(VotoDTO votoDTO) {
        try {
            return associadoService.procurarPorId(votoDTO.getCpf())
                    .switchIfEmpty(Mono.error(new ApplicationException("Associado não encontrado")))
                    .map(a -> {
                        votoDTO.setAssociado(a);
                        return votoDTO;
                    })
                    .flatMap(this::verificarVotoExistente)
                    .flatMap(this::verificarAssociadoHabilitado);
        } catch (ApplicationException e) {
            return Mono.error(e);
        }
    }

    private Mono<VotoDTO> verificarAssociadoHabilitado(VotoDTO votoDTO) {
        WebClient.RequestHeadersUriSpec<?> getMethod = webClientCPF.get();
        WebClient.ResponseSpec responseUri = getMethod.uri("/users/" + votoDTO.getAssociado().getCpf()).retrieve();
        return responseUri
                .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new ApplicationException("CPF Inválido"))))
                .bodyToMono(StatusCPFDTO.class)
                .flatMap(s -> validarRetornoCPF(s, votoDTO));
    }

    private Mono<VotoDTO> validarRetornoCPF(StatusCPFDTO statusCPFDTO, VotoDTO votoDTO) {
        if(!statusCPFDTO.getStatus().equals("ABLE_TO_VOTE")) {
            return Mono.error(new ApplicationException("Associado não habilitado para voto"));
        }
        return Mono.just(votoDTO);
    }

    private Mono<VotoDTO> verificarVotoExistente(VotoDTO votoDTO) {
        Associado associado = votoDTO.getAssociado();
        Votacao votacao = votoDTO.getPauta().buscarVotacaoAberta();

        boolean possuiVoto = votacao.getVotoList().stream().anyMatch(
                e -> e.getAssociado().getCpf().equals(associado.getCpf()));

        if (possuiVoto) {
            return Mono.error(new ApplicationException("Associado já registrou voto para a pauta"));
        }
        return Mono.just(votoDTO);
    }

    private Mono<VotoDTO> validarPautaParaVoto(VotoDTO votoDTO) {
        if(!votoDTO.getPauta().temVotacaoEmAndamento()) {
            return Mono.error(new ApplicationException("Pauta não possui votação em andamento"));
        }
        return Mono.just(votoDTO);
    }

    private void validarVotoDTO(VotoDTO votoDTO) throws ApplicationException {
        if (votoDTO == null) {
            throw new ApplicationException("Objeto para registro do voto não instanciado");
        }
        if (votoDTO.getCodigoPauta() == null) {
            throw new ApplicationException("Informações da pauta não informadas");
        }
        if (votoDTO.getCpf() == null) {
            throw new ApplicationException("Informações do associado não informadas");
        }
        if (votoDTO.getOpcao() == null) {
            throw new ApplicationException("Resposta do voto não informada");
        }
    }

    public Mono<Votacao> abrirVotacao(SessaoDTO sessaoDTO)  throws ApplicationException {
        validarSessaoDTO(sessaoDTO);
        Mono<Pauta> pautaComVotacaoAberta = procurarPorId(sessaoDTO.getCodigoPauta())
                .switchIfEmpty(Mono.error(new ApplicationException("Pauta não encontrada")))
                .map(p -> {
                    sessaoDTO.setPauta(p);
                    return sessaoDTO;
                })
                .flatMap(this::validarPauta);
        return pautaComVotacaoAberta.flatMap(this::aplicarTimeout);
    }

    public Mono<Pauta> atualizarPauta(Pauta pauta) {
        try {
            return atualizar(pauta);
        } catch (ApplicationException e) {
            return Mono.error(e);
        }
    }

    private Mono<Votacao> aplicarTimeout(Pauta pauta) {
        Votacao votacao = pauta.buscarVotacaoAberta();
        Duration duration = Duration.ofMinutes(votacao.getTimeout());
        Mono<Votacao> votacaoMono = Mono.just(votacao);
        votacaoMono.subscribeOn(Schedulers.parallel())
                .delayElement(duration)
                .map(v -> pauta)
                .flatMap(this::encerrarVotacao)
                .subscribe();
        return votacaoMono;
    }

    private Mono<Votacao> encerrarVotacao(Pauta pauta) {
        return getRepository().findById(pauta.getCodigo())
                .map(p -> {
                    Votacao votacao = p.buscarVotacaoAberta();
                    votacao.setFinalizada(true);
                    return p;
                })
                .flatMap(getRepository()::save)
                .map(Pauta::buscarUltimaVotacao)
                .flatMap(Mono::just);
    }

    private Mono<Pauta> validarPauta(SessaoDTO sessaoDTO) {
        if(sessaoDTO.getPauta().temVotacaoEmAndamento()) {
            return Mono.error(new ApplicationException("Pauta com votação em andamento"));
        }
        Pauta p = sessaoDTO.getPauta();
        Votacao votacao = new Votacao();
        votacao.setDataInicio(new Date());
        Long duracao = sessaoDTO.getDuracao() != null ? sessaoDTO.getDuracao() : 1;
        votacao.setTimeout(duracao);
        p.getVotacaoList().add(votacao);
        return Mono.just(p).flatMap(this::atualizarPauta);
    }

    public void validarSessaoDTO(SessaoDTO sessaoDTO) throws ApplicationException {
        if (sessaoDTO == null || sessaoDTO.getCodigoPauta() == null) {
            throw new ApplicationException("Informações da pauta não informadas");
        }
    }

    @Override
    public void validarEntidadeInserir(Pauta entity) throws ApplicationException {
        if (entity == null) {
            throw new ApplicationException("Objeto não instanciado");
        }
        if (entity.getId() == null) {
            throw new ApplicationException("Código da Pauta não informado");
        }
        entity.setVotacaoList(new ArrayList<>());
    }

    @Override
    public void validarEntidadeAtualizar(Pauta entity) throws ApplicationException {
        if (entity == null) {
            throw new ApplicationException("Objeto não instanciado");
        }
        if (entity.getId() == null) {
            throw new ApplicationException("Código da Pauta não informado");
        }
    }
}
