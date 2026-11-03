package br.pjbl.minicrm.oportunidades.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OportunidadeJpaRepository extends JpaRepository<OportunidadeEntity, Long> {

    List<OportunidadeEntity> findAllByOrderByIdDesc();

    List<OportunidadeEntity> findByClienteIdOrderByIdDesc(String clienteId);

    List<OportunidadeEntity> findByResponsavelIdInOrderByIdDesc(Collection<String> responsaveis);

    List<OportunidadeEntity> findByClienteIdAndResponsavelIdInOrderByIdDesc(String clienteId, Collection<String> responsaveis);
}
