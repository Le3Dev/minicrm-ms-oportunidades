package br.pjbl.minicrm.oportunidades.application.atualizaroportunidade;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

import java.time.Clock;

/** Edita título, valor, data prevista e responsável; só oportunidades abertas (a regra fica na entidade). */
public class AtualizarOportunidadeHandler {

    private final OportunidadeRepository repositorio;
    private final Clock relogio;

    public AtualizarOportunidadeHandler(OportunidadeRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public OportunidadeView executar(AtualizarOportunidadeCommand c) {
        Oportunidade oportunidade = repositorio.buscarPorId(c.id()).orElseThrow(() -> new OportunidadeNaoEncontradaException(c.id()));
        oportunidade.atualizar(c.titulo(), c.valor(), c.dataPrevista(), c.responsavelId(), relogio.instant());
        return OportunidadeView.de(repositorio.salvar(oportunidade));
    }
}
