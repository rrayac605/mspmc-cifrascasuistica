package mx.gob.imss.cit.pmc.cifrascasuistica.services;

import java.util.List;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlMovimientosResponseDTO;
import org.springframework.data.domain.Page;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.DetalleSalidaOutput;


public interface MsPmcCifrasControlService {

	List<CifrasControlMovimientosResponseDTO> getCifrasControl(MsPmcCifrasControlInput input) throws BusinessException;

	MovimientoCasuisticaOutput getMovimientoCasuistica(MsPmcCifrasControlInput input) throws BusinessException;

}

