package br.pjbl.minicrm.oportunidades.application.excluiroportunidade;

import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

/** Exclui a oportunidade, exceto se estiver fechada como ganha (a regra fica na entidade). */
public class ExcluirOportunidadeHandler {

    private final OportunidadeRepository repositorio;

    public ExcluirOportunidadeHandler(OportunidadeRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(long id) {
        Oportunidade oportunidade = repositorio.buscarPorId(id).orElseThrow(() -> new OportunidadeNaoEncontradaException(id));
        oportunidade.verificarExclusao();
        repositorio.excluir(id);
    }
}
