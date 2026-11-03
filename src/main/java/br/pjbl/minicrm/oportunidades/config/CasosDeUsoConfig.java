package br.pjbl.minicrm.oportunidades.config;

import br.pjbl.minicrm.oportunidades.application.atualizaroportunidade.AtualizarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.buscaroportunidade.BuscarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.excluiroportunidade.ExcluirOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.listarfunil.ListarFunilHandler;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Raiz de composição dos casos de uso (classes Java puras). */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    CriarOportunidadeHandler criarOportunidade(OportunidadeRepository r, Clock c) {
        return new CriarOportunidadeHandler(r, c);
    }

    @Bean
    MoverOportunidadeHandler moverOportunidade(OportunidadeRepository r, EventoPublisher e, Clock c) {
        return new MoverOportunidadeHandler(r, e, c);
    }

    @Bean
    ListarFunilHandler listarFunil(OportunidadeRepository r) {
        return new ListarFunilHandler(r);
    }

    @Bean
    BuscarOportunidadeHandler buscarOportunidade(OportunidadeRepository r) {
        return new BuscarOportunidadeHandler(r);
    }

    @Bean
    AtualizarOportunidadeHandler atualizarOportunidade(OportunidadeRepository r, Clock c) {
        return new AtualizarOportunidadeHandler(r, c);
    }

    @Bean
    ExcluirOportunidadeHandler excluirOportunidade(OportunidadeRepository r) {
        return new ExcluirOportunidadeHandler(r);
    }
}
