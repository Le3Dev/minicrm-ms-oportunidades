package br.pjbl.minicrm.oportunidades.application.listarfunil;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.domain.Etapa;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** RF08, RF19: monta o quadro do funil com contagem e soma por etapa, respeitando o escopo (RF15) que o BFF envia. */
public class ListarFunilHandler {

    private final OportunidadeRepository repositorio;

    public ListarFunilHandler(OportunidadeRepository repositorio) {
        this.repositorio = repositorio;
    }

    public FunilView executar(ListarFunilQuery q) {
        List<Oportunidade> todas = repositorio.listar(q.clienteId(), q.responsaveis());
        List<FunilView.Coluna> colunas = new ArrayList<>();
        for (Etapa etapa : Etapa.values()) {
            List<OportunidadeView> itens = todas.stream().filter(o -> o.etapa() == etapa).map(OportunidadeView::de).toList();
            colunas.add(new FunilView.Coluna(etapa.rotulo(), itens.size(), soma(itens), itens));
        }
        BigDecimal emNegociacao = colunas.stream().filter(c -> !c.etapa().equals(Etapa.FECHADO.rotulo()))
                .map(FunilView.Coluna::valorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FunilView(colunas, todas.size(), soma(todas.stream().map(OportunidadeView::de).toList()), emNegociacao);
    }

    private static BigDecimal soma(List<OportunidadeView> itens) {
        return itens.stream().map(OportunidadeView::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
