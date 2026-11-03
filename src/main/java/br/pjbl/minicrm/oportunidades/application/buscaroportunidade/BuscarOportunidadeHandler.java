package br.pjbl.minicrm.oportunidades.application.buscaroportunidade;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

/** Busca uma oportunidade pelo id; 404 se não existir. */
public class BuscarOportunidadeHandler {

    private final OportunidadeRepository repositorio;

    public BuscarOportunidadeHandler(OportunidadeRepository repositorio) {
        this.repositorio = repositorio;
    }

    public OportunidadeView executar(long id) {
        return repositorio.buscarPorId(id)
                .map(OportunidadeView::de)
                .orElseThrow(() -> new OportunidadeNaoEncontradaException(id));
    }
}
