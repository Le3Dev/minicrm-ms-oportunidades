package br.pjbl.minicrm.oportunidades.application.atualizaroportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtualizarOportunidadeCommand(long id, String titulo, BigDecimal valor, LocalDate dataPrevista, String responsavelId) {
}
