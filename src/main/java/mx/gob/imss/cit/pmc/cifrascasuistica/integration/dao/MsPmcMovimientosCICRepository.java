package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;

public interface MsPmcMovimientosCICRepository {
	
	MovimientoCasuisticaOutput getMovimientosCasuistica(MsPmcCifrasControlInput input) throws BusinessException;

}
