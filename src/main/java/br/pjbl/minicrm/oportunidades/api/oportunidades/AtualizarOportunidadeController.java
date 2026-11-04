package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.atualizaroportunidade.AtualizarOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.atualizaroportunidade.AtualizarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class AtualizarOportunidadeController {

    private final AtualizarOportunidadeHandler handler;

    public AtualizarOportunidadeController(AtualizarOportunidadeHandler handler) {
        this.handler = handler;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza título, valor, data prevista e responsável; só se estiver aberta")
    public OportunidadeView atualizar(@PathVariable long id, @Valid @RequestBody OportunidadeRequests.Atualizacao a) {
        return handler.executar(new AtualizarOportunidadeCommand(id, a.titulo(), a.valor(), a.dataPrevista(), a.responsavelId()));
    }
}
