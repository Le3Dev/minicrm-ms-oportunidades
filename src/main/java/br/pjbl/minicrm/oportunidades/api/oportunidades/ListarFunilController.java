package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.listarfunil.FunilView;
import br.pjbl.minicrm.oportunidades.application.listarfunil.ListarFunilHandler;
import br.pjbl.minicrm.oportunidades.application.listarfunil.ListarFunilQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class ListarFunilController {

    private final ListarFunilHandler handler;

    public ListarFunilController(ListarFunilHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @Operation(summary = "Lista o funil com as 4 etapas, quantidade e soma por etapa (RF08, RF19)")
    public FunilView listar(
            @Parameter(description = "Ids dos responsáveis visíveis, separados por vírgula (RF15)", example = "u2,u5")
            @RequestParam(required = false) String responsavelId,
            @Parameter(description = "Filtra pelo cliente") @RequestParam(required = false) String clienteId) {
        List<String> responsaveis = responsavelId == null ? List.of()
                : Arrays.stream(responsavelId.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return handler.executar(new ListarFunilQuery(clienteId, responsaveis));
    }
}
