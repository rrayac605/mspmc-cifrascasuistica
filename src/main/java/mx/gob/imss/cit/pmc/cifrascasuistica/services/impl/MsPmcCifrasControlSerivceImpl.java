package mx.gob.imss.cit.pmc.cifrascasuistica.services.impl;

import java.util.List;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlMovimientosResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.MsPmcCifrasControlRepository;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.DetalleSalidaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.MsPmcMovimientosCICRepositoryImpl;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.MsPmcCifrasControlService;


@Service("msPmcCifrasControlService")
public class MsPmcCifrasControlSerivceImpl implements MsPmcCifrasControlService {
	
	@Autowired
	private MsPmcCifrasControlRepository msPmcCifrasControlRepository;
	
	@Autowired
	private MsPmcMovimientosCICRepositoryImpl movimientosCICRepository;
	
	@Override
	public List<CifrasControlMovimientosResponseDTO> getCifrasControl(MsPmcCifrasControlInput input) throws BusinessException {
		return msPmcCifrasControlRepository.getCifrasControl(input);
	}

	@Override
	public MovimientoCasuisticaOutput getMovimientoCasuistica(MsPmcCifrasControlInput input)
			throws BusinessException {
		MovimientoCasuisticaOutput movimientosCasuistica = movimientosCICRepository.getMovimientosCasuistica(input);
		return movimientosCasuistica;
	}

}
