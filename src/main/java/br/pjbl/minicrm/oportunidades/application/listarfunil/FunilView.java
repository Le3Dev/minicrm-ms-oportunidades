package br.pjbl.minicrm.oportunidades.application.listarfunil;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;

import java.math.BigDecimal;
import java.util.List;

/** Funil com as 4 etapas na ordem do RF08, cada uma com itens, quantidade e soma dos valores (RF19). */
public record FunilView(List<Coluna> etapas, int quantidade, BigDecimal valorTotal, BigDecimal valorEmNegociacao) {

    public record Coluna(String etapa, int quantidade, BigDecimal valorTotal, List<OportunidadeView> itens) {
    }
}
