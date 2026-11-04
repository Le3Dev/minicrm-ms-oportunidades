package br.pjbl.minicrm.oportunidades.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.infrastructure.eventos.LogEventoPublisher;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Aplicação inteira com H2 (modo SQL Server) e a migração Flyway real. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OportunidadesApiTest {

    @Autowired
    private MockMvc http;
    @Autowired
    private EventoPublisher eventos;

    @Test
    void criaFechaComoGanhoEPublicaOEvento() throws Exception {
        String corpo = http.perform(post("/oportunidades").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId":"c1","titulo":"Plano anual","valor":18500,"dataPrevista":"2026-12-15","responsavelId":"u2"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapa").value("Prospecção"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(corpo, "$.id");

        http.perform(patch("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Fechado\",\"resultado\":\"ganho\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("ganho"));

        assertThat(((LogEventoPublisher) eventos).publicados()).anySatisfy(e -> assertThat(e.oportunidadeId()).isEqualTo(id.longValue()));

        http.perform(patch("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Contato\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOVIMENTACAO_INVALIDA"));
    }

    @Test
    void validaEntradaEDocumentaNoSwagger() throws Exception {
        http.perform(post("/oportunidades").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"A\",\"valor\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
        http.perform(patch("/oportunidades/999").contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Contato\"}"))
                .andExpect(status().isNotFound());
        http.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/oportunidades/{id}']").exists());
    }

    private Number criar(String clienteId, String titulo, String valor, String responsavel) throws Exception {
        String corpo = http.perform(post("/oportunidades").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"clienteId\":\"" + clienteId + "\",\"titulo\":\"" + titulo + "\",\"valor\":" + valor + ",\"responsavelId\":\"" + responsavel + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    @Test
    void buscaPorIdEDevolve404() throws Exception {
        Number id = criar("c-busca", "Plano busca", "100", "u2");

        http.perform(get("/oportunidades/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Plano busca"))
                .andExpect(jsonPath("$.etapa").value("Prospecção"));
        http.perform(get("/oportunidades/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }

    @Test
    void atualizaAbertaERecusaFechada() throws Exception {
        Number id = criar("c-edita", "Plano edita", "100", "u2");

        http.perform(put("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Plano editado\",\"valor\":250.5,\"dataPrevista\":\"2026-12-20\",\"responsavelId\":\"u5\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Plano editado"))
                .andExpect(jsonPath("$.valor").value(250.5))
                .andExpect(jsonPath("$.responsavelId").value("u5"));
        http.perform(put("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"A\",\"valor\":0,\"responsavelId\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACAO"));
        http.perform(put("/oportunidades/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Plano\",\"valor\":10,\"responsavelId\":\"u2\"}"))
                .andExpect(status().isNotFound());

        http.perform(patch("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Fechado\",\"resultado\":\"perdido\",\"motivoPerda\":\"Preço\"}"))
                .andExpect(status().isOk());
        http.perform(put("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Depois de fechada\",\"valor\":10,\"responsavelId\":\"u2\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("MOVIMENTACAO_INVALIDA"));
    }

    @Test
    void excluiExcetoFechadaComoGanha() throws Exception {
        Number aberta = criar("c-exclui", "Plano aberta", "100", "u2");
        Number ganha = criar("c-exclui", "Plano ganha", "200", "u2");
        http.perform(patch("/oportunidades/" + ganha).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Fechado\",\"resultado\":\"ganho\"}"))
                .andExpect(status().isOk());

        http.perform(delete("/oportunidades/" + aberta)).andExpect(status().isNoContent());
        http.perform(get("/oportunidades/" + aberta)).andExpect(status().isNotFound());
        http.perform(delete("/oportunidades/" + ganha)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOVIMENTACAO_INVALIDA"));
        http.perform(get("/oportunidades/" + ganha)).andExpect(status().isOk());
        http.perform(delete("/oportunidades/999999")).andExpect(status().isNotFound());
    }

    @Test
    void listaOFunilFiltrandoPorClienteEResponsaveis() throws Exception {
        criar("c-funil", "Prospecção funil", "1000", "u2");
        Number contato = criar("c-funil", "Contato funil", "2000", "u5");
        criar("c-funil", "Outro vendedor", "500", "u4");
        http.perform(patch("/oportunidades/" + contato).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Contato\"}"))
                .andExpect(status().isOk());

        http.perform(get("/oportunidades").param("clienteId", "c-funil").param("responsavelId", "u2,u5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapas.length()").value(4))
                .andExpect(jsonPath("$.etapas[0].etapa").value("Prospecção"))
                .andExpect(jsonPath("$.etapas[0].quantidade").value(1))
                .andExpect(jsonPath("$.etapas[0].valorTotal").value(1000.0))
                .andExpect(jsonPath("$.etapas[1].etapa").value("Contato"))
                .andExpect(jsonPath("$.etapas[1].itens[0].titulo").value("Contato funil"))
                .andExpect(jsonPath("$.etapas[3].etapa").value("Fechado"))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.valorEmNegociacao").value(3000.0));
        http.perform(get("/oportunidades").param("clienteId", "c-funil"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(3));
        http.perform(get("/oportunidades").param("clienteId", "nao-existe"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(0));
    }
}
