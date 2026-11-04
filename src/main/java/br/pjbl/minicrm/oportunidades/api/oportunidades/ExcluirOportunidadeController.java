package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.excluiroportunidade.ExcluirOportunidadeHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class ExcluirOportunidadeController {

    private final ExcluirOportunidadeHandler handler;

    public ExcluirOportunidadeController(ExcluirOportunidadeHandler handler) {
        this.handler = handler;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui a oportunidade; proibido se estiver fechada como ganha")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        handler.executar(id);
        return ResponseEntity.noContent().build();
    }
}
