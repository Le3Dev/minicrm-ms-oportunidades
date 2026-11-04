package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.buscaroportunidade.BuscarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class BuscarOportunidadeController {

    private final BuscarOportunidadeHandler handler;

    public BuscarOportunidadeController(BuscarOportunidadeHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma oportunidade pelo id (404 se não existir)")
    public OportunidadeView buscar(@PathVariable long id) {
        return handler.executar(id);
    }
}
