package br.pjbl.minicrm.oportunidades.application.listarfunil;

import java.util.List;

/** Filtros opcionais: {@code clienteId} vazio e {@code responsaveis} vazio significam sem recorte. */
public record ListarFunilQuery(String clienteId, List<String> responsaveis) {
}
