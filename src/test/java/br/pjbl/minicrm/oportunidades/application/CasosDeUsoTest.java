package br.pjbl.minicrm.oportunidades.application;

import br.pjbl.minicrm.oportunidades.application.listarfunil.ListarFunilQuery;
import br.pjbl.minicrm.oportunidades.application.listarfunil.ListarFunilHandler;
import br.pjbl.minicrm.oportunidades.application.listarfunil.FunilView;
import br.pjbl.minicrm.oportunidades.application.excluiroportunidade.ExcluirOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.buscaroportunidade.BuscarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.atualizaroportunidade.AtualizarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.atualizaroportunidade.AtualizarOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.domain.MovimentacaoInvalidaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeGanha;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.infrastructure.memoria.InMemoryOportunidadeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
    private final InMemoryOportunidadeRepository repositorio = new InMemoryOportunidadeRepository();
    private final List<OportunidadeGanha> publicados = new ArrayList<>();
    private final MoverOportunidadeHandler mover = new MoverOportunidadeHandler(repositorio, publicados::add, relogio);

    private long criar() {
        return new CriarOportunidadeHandler(repositorio, relogio)
                .executar(new CriarOportunidadeCommand("c1", "Plano anual", new BigDecimal("18500"), null, "u2", "Proposta")).id();
    }

    @Test
    void fecharComoGanhoGravaEPublicaOEvento() {
        long id = criar();

        var view = mover.executar(new MoverOportunidadeCommand(id, "Fechado", "ganho", null, false));

        assertThat(view.etapa()).isEqualTo("Fechado");
        assertThat(view.resultado()).isEqualTo("ganho");
        assertThat(publicados).singleElement().satisfies(e -> {
            assertThat(e.oportunidadeId()).isEqualTo(id);
            assertThat(e.clienteId()).isEqualTo("c1");
        });
        assertThat(repositorio.buscarPorId(id).orElseThrow().versao()).isEqualTo(1L);
    }

    @Test
    void perderOuMoverNaoPublica() {
        long id = criar();
        mover.executar(new MoverOportunidadeCommand(id, "Contato", null, null, false));
        mover.executar(new MoverOportunidadeCommand(id, "Fechado", "perdido", "Sem verba", false));
        assertThat(publicados).isEmpty();
    }

    @Test
    void movimentacaoInvalidaNaoGravaNemPublica() {
        long id = criar();
        mover.executar(new MoverOportunidadeCommand(id, "Fechado", "ganho", null, false));

        assertThatThrownBy(() -> mover.executar(new MoverOportunidadeCommand(id, "Proposta", null, null, false)))
                .isInstanceOf(MovimentacaoInvalidaException.class);
        assertThat(publicados).hasSize(1);
        assertThatThrownBy(() -> mover.executar(new MoverOportunidadeCommand(999, "Contato", null, null, false)))
                .isInstanceOf(OportunidadeNaoEncontradaException.class);
    }

    private final BuscarOportunidadeHandler buscar = new BuscarOportunidadeHandler(repositorio);
    private final AtualizarOportunidadeHandler atualizar = new AtualizarOportunidadeHandler(repositorio, relogio);
    private final ExcluirOportunidadeHandler excluir = new ExcluirOportunidadeHandler(repositorio);
    private final ListarFunilHandler funil = new ListarFunilHandler(repositorio);

    private long criar(String clienteId, String valor, String responsavel, String etapa) {
        return new CriarOportunidadeHandler(repositorio, relogio)
                .executar(new CriarOportunidadeCommand(clienteId, "Oportunidade " + valor, new BigDecimal(valor), null, responsavel, etapa)).id();
    }

    @Test
    void buscaPorIdEDevolve404SeNaoExiste() {
        long id = criar();

        assertThat(buscar.executar(id).titulo()).isEqualTo("Plano anual");
        assertThatThrownBy(() -> buscar.executar(999)).isInstanceOf(OportunidadeNaoEncontradaException.class);
    }

    @Test
    void atualizaSoQuandoAberta() {
        long id = criar();

        var view = atualizar.executar(new AtualizarOportunidadeCommand(id, "Plano anual v2", new BigDecimal("20000"), null, "u5"));

        assertThat(view.titulo()).isEqualTo("Plano anual v2");
        assertThat(view.responsavelId()).isEqualTo("u5");
        assertThat(repositorio.buscarPorId(id).orElseThrow().versao()).isEqualTo(1L);

        mover.executar(new MoverOportunidadeCommand(id, "Fechado", "perdido", "Preço", false));
        assertThatThrownBy(() -> atualizar.executar(new AtualizarOportunidadeCommand(id, "Outro", new BigDecimal("1"), null, "u2")))
                .isInstanceOf(MovimentacaoInvalidaException.class);
        assertThatThrownBy(() -> atualizar.executar(new AtualizarOportunidadeCommand(999, "Outro", new BigDecimal("1"), null, "u2")))
                .isInstanceOf(OportunidadeNaoEncontradaException.class);
    }

    @Test
    void excluiExcetoGanha() {
        long aberta = criar();
        excluir.executar(aberta);
        assertThat(repositorio.buscarPorId(aberta)).isEmpty();

        long ganha = criar();
        mover.executar(new MoverOportunidadeCommand(ganha, "Fechado", "ganho", null, false));
        assertThatThrownBy(() -> excluir.executar(ganha)).isInstanceOf(MovimentacaoInvalidaException.class);
        assertThat(repositorio.buscarPorId(ganha)).isPresent();

        long perdida = criar();
        mover.executar(new MoverOportunidadeCommand(perdida, "Fechado", "perdido", "Sem verba", false));
        excluir.executar(perdida);
        assertThat(repositorio.buscarPorId(perdida)).isEmpty();
        assertThatThrownBy(() -> excluir.executar(999)).isInstanceOf(OportunidadeNaoEncontradaException.class);
    }

    @Test
    void funilTemAsQuatroEtapasComContagemESoma() {
        criar("c1", "1000", "u2", "Prospecção");
        criar("c1", "2000", "u2", "Contato");
        criar("c2", "500", "u5", "Contato");
        long fechada = criar("c2", "300", "u5", "Proposta");
        mover.executar(new MoverOportunidadeCommand(fechada, "Fechado", "ganho", null, false));

        var todos = funil.executar(new ListarFunilQuery(null, List.of()));

        assertThat(todos.etapas()).extracting(FunilView.Coluna::etapa).containsExactly("Prospecção", "Contato", "Proposta", "Fechado");
        assertThat(todos.etapas()).extracting(FunilView.Coluna::quantidade).containsExactly(1, 2, 0, 1);
        assertThat(todos.etapas().get(1).valorTotal()).isEqualByComparingTo("2500");
        assertThat(todos.quantidade()).isEqualTo(4);
        assertThat(todos.valorTotal()).isEqualByComparingTo("3800");
        assertThat(todos.valorEmNegociacao()).isEqualByComparingTo("3500");
    }

    @Test
    void funilRespeitaEscopoEFiltroDeCliente() {
        criar("c1", "1000", "u2", "Prospecção");
        criar("c1", "2000", "u5", "Contato");
        criar("c2", "500", "u4", "Contato");

        var doEscopo = funil.executar(new ListarFunilQuery(null, List.of("u2", "u5")));
        assertThat(doEscopo.quantidade()).isEqualTo(2);

        var porCliente = funil.executar(new ListarFunilQuery("c2", null));
        assertThat(porCliente.quantidade()).isEqualTo(1);
        assertThat(porCliente.etapas().get(1).itens()).singleElement().satisfies(o -> assertThat(o.responsavelId()).isEqualTo("u4"));

        var combinado = funil.executar(new ListarFunilQuery("c1", List.of("u5")));
        assertThat(combinado.quantidade()).isEqualTo(1);
        assertThat(combinado.valorTotal()).isEqualByComparingTo("2000");

        assertThat(funil.executar(new ListarFunilQuery(null, List.of("u8"))).quantidade()).isZero();
    }
}
