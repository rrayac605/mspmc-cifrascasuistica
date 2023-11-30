package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaDTO;
import mx.gob.imss.cit.pmc.cifrascasuistica.security.model.CifrasControlDTO;

public class DetalleSalidaOutput {
	
	
	@Getter
	@Setter
	private List<DetalleConsultaDTO> detalleConsultaDTO;
	@Getter
	@Setter
	private CifrasControlDTO cifrasControlTotales;

}
