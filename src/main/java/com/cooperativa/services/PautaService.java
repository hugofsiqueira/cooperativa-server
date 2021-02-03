package com.cooperativa.services;

import com.cooperativa.dto.SessaoDTO;
import com.cooperativa.dto.StatusCPFDTO;
import com.cooperativa.dto.VotoDTO;
import com.cooperativa.exceptions.ApplicationException;
import com.cooperativa.model.*;
import com.cooperativa.repositories.PautaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;

@Service
public class PautaService extends GenericServiceImpl<Pauta, String, PautaRepository> {

    private WebClient webClientCPF;

    @Autowired
    private ResultadoPautaSender resultadoPautaSender;

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

    public Mono<Pauta> abrirVotacao(SessaoDTO sessaoDTO) throws ApplicationException {
      validarSessaoDTO(sessaoDTO);
      return procurarPorId(sessaoDTO.getCodigoPauta())
        .switchIfEmpty(Mono.error(new ApplicationException("Pauta não encontrada")))
        .map(p -> {
          sessaoDTO.setPauta(p);
          return sessaoDTO;
        })
        .flatMap(this::validarPauta)
        .flatMap(this::aplicarTimeout);
    }

    private Mono<Voto> salvarVotoEAtualizarPauta(VotoDTO votoDTO) {
        Pauta pauta = votoDTO.getPauta();
        Votacao votacao = pauta.getVotacao();

        Voto voto = new Voto();
        Associado associado = new Associado();
        associado.setCpf(votoDTO.getCpf());
        voto.setAssociado(associado);
        voto.setOpcao(votoDTO.getOpcao());

        votacao.getVotoList().add(voto);
        return getRepository().save(pauta)
                .map(p -> voto)
                .flatMap(Mono::just);
    }

    private Mono<VotoDTO> validarAssociadoParaVoto(VotoDTO votoDTO) {
        return verificarVotoExistente(votoDTO)
                   .flatMap(this::verificarAssociadoHabilitado);
    }

    private Mono<VotoDTO> verificarAssociadoHabilitado(VotoDTO votoDTO) {
        WebClient.RequestHeadersUriSpec<?> getMethod = webClientCPF.get();
        WebClient.ResponseSpec responseUri = getMethod.uri("/users/" + votoDTO.getCpf()).retrieve();
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
        String cpfAssociado = votoDTO.getCpf();
        Votacao votacao = votoDTO.getPauta().getVotacao();

        boolean possuiVoto = votacao.getVotoList().stream().anyMatch(
                e -> e.getAssociado().getCpf().equals(cpfAssociado));

        if (possuiVoto) {
            return Mono.error(new ApplicationException("Associado já registrou voto para a pauta"));
        }
        return Mono.just(votoDTO);
    }

    private Mono<VotoDTO> validarPautaParaVoto(VotoDTO votoDTO) {
        Votacao votacao = votoDTO.getPauta().getVotacao();
        if (votacao != null && votacao.isFinalizada()) {
          return Mono.error(new ApplicationException("Pauta não possui votação em andamento"));
        }
        return Mono.just(votoDTO);
    }

    private void validarVotoDTO(VotoDTO votoDTO) throws ApplicationException {
        if (votoDTO == null) {
            throw new ApplicationException("Objeto para registro do voto não instanciado");
        }
        if (votoDTO.getCodigoPauta() == null || votoDTO.getCodigoPauta().trim().isEmpty()) {
            throw new ApplicationException("Código da pauta não informado");
        }
        if (votoDTO.getCpf() == null || votoDTO.getCpf().trim().isEmpty()) {
            throw new ApplicationException("CPF do associado não informado");
        }
        if (votoDTO.getOpcao() == null || votoDTO.getOpcao().trim().isEmpty()) {
            throw new ApplicationException("Resposta do voto não informada");
        }
        if (!votoDTO.getOpcao().equals(OpcaoVoto.SIM.getLabel()) &&
          !votoDTO.getOpcao().equals(OpcaoVoto.NAO.getLabel())) {
          throw new ApplicationException("Resposta do voto deve ser Sim ou Não");
        }
    }

    private Mono<Pauta> atualizarPauta(Pauta pauta) {
        try {
            return atualizar(pauta);
        } catch (ApplicationException e) {
            return Mono.error(e);
        }
    }

    private Mono<Pauta> aplicarTimeout(Pauta pauta) {
        Votacao votacao = pauta.getVotacao();
        Duration duration = Duration.ofMinutes(votacao.getDuracaoMinutos());
        Mono<Votacao> votacaoMono = Mono.just(votacao);
        votacaoMono.subscribeOn(Schedulers.parallel())
                .delayElement(duration)
                .map(v -> pauta)
                .flatMap(this::encerrarVotacao)
                .subscribe();
        return Mono.just(pauta);
    }

    private Mono<Votacao> encerrarVotacao(Pauta pauta) {
        return getRepository().findById(pauta.getCodigo())
                .map(p -> {
                    Votacao votacao = p.getVotacao();
                    votacao.setFinalizada(true);
                    votacao.apurarResultado();
                    return p;
                })
                .flatMap(getRepository()::save)
                .flatMap(resultadoPautaSender::enviarResultado)
                .map(Pauta::getVotacao)
                .flatMap(Mono::just);
    }

    private Mono<Pauta> validarPauta(SessaoDTO sessaoDTO) {
        Pauta pauta = sessaoDTO.getPauta();
        if(pauta.temVotacaoEmAndamento()) {
            return Mono.error(new ApplicationException("Pauta com votação em andamento"));
        }
        if(pauta.temVotacaoFinalizada()) {
          return Mono.error(new ApplicationException("Pauta com votação realizada"));
        }
        Votacao votacao = new Votacao();
        votacao.setDataInicio(new Date());
        Long duracao = sessaoDTO.getDuracaoMinutos() != null ? sessaoDTO.getDuracaoMinutos() : 1;
        votacao.setDuracaoMinutos(duracao);
        pauta.setVotacao(votacao);
        return Mono.just(pauta).flatMap(this::atualizarPauta);
    }

    private void validarSessaoDTO(SessaoDTO sessaoDTO) throws ApplicationException {
        if (sessaoDTO == null || sessaoDTO.getCodigoPauta() == null) {
            throw new ApplicationException("Informações da pauta não informadas");
        }
    }

    @Override
    public void validarId(String id) throws ApplicationException {
      if (id == null || id.trim().isEmpty()) {
        throw new ApplicationException("Código da pauta não informado");
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
        entity.setDataCriacao(new Date());
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
