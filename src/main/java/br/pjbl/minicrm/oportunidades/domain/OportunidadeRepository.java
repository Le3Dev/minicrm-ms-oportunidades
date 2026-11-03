package br.pjbl.minicrm.oportunidades.domain;

import java.util.List;
import java.util.Optional;

/** Porta de persistência (Azure SQL na infraestrutura). */
public interface OportunidadeRepository {

    /** Insere (id nulo) ou atualiza, devolvendo a oportunidade com id e versão. */
    Oportunidade salvar(Oportunidade oportunidade);

    Optional<Oportunidade> buscarPorId(long id);

    /**
     * Lista para o funil (RF08, RF15), mais recentes primeiro.
     *
     * @param clienteId    filtra por cliente; nulo ou vazio = todos
     * @param responsaveis ids dos responsáveis visíveis; nulo ou vazio = todos
     */
    List<Oportunidade> listar(String clienteId, List<String> responsaveis);

    /** Remove pelo id; não faz nada se não existir. */
    void excluir(long id);
}
