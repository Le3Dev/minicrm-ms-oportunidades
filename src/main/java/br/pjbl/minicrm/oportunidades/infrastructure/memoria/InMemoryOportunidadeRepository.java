package br.pjbl.minicrm.oportunidades.infrastructure.memoria;

import br.pjbl.minicrm.oportunidades.domain.ConcorrenciaException;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Adaptador em memória (testes de casos de uso). */
public class InMemoryOportunidadeRepository implements OportunidadeRepository {

    private final Map<Long, Oportunidade.Estado> registros = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    @Override
    public synchronized Oportunidade salvar(Oportunidade o) {
        Oportunidade.Estado e = o.estado();
        long id = e.id() == null ? sequencia.incrementAndGet() : e.id();
        Oportunidade.Estado atual = registros.get(id);
        if (!Objects.equals(atual == null ? null : atual.versao(), e.versao())) {
            throw new ConcorrenciaException(id);
        }
        long versao = e.versao() == null ? 0 : e.versao() + 1;
        Oportunidade.Estado gravado = new Oportunidade.Estado(id, e.clienteId(), e.titulo(), e.etapa(), e.resultado(), e.motivoPerda(),
                e.valor(), e.dataPrevista(), e.responsavelId(), e.criadaEm(), e.atualizadaEm(), versao);
        registros.put(id, gravado);
        return Oportunidade.restaurar(gravado);
    }

    @Override
    public Optional<Oportunidade> buscarPorId(long id) {
        return Optional.ofNullable(registros.get(id)).map(Oportunidade::restaurar);
    }

    @Override
    public List<Oportunidade> listar(String clienteId, List<String> responsaveis) {
        boolean porCliente = clienteId != null && !clienteId.isBlank();
        boolean porResponsaveis = responsaveis != null && !responsaveis.isEmpty();
        return registros.values().stream()
                .filter(e -> !porCliente || e.clienteId().equals(clienteId))
                .filter(e -> !porResponsaveis || responsaveis.contains(e.responsavelId()))
                .sorted(Comparator.comparing(Oportunidade.Estado::id).reversed())
                .map(Oportunidade::restaurar)
                .toList();
    }

    @Override
    public void excluir(long id) {
        registros.remove(id);
    }
}
