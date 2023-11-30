package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao;

import java.util.Optional;

import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;


public interface ParametroRepository {

	Optional<ParametroDTO> findOneByCve(String cveIdParametro);

}
