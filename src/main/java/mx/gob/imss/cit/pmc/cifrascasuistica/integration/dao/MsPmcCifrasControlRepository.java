package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao;

import java.util.List;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlMovimientosResponseDTO;
import org.springframework.data.domain.Page;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.DetalleSalidaOutput;

public interface MsPmcCifrasControlRepository {

	List<CifrasControlMovimientosResponseDTO> getCifrasControl(MsPmcCifrasControlInput input) throws BusinessException;

}
