package mx.gob.imss.cit.pmc.cifrascasuistica.integration.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class FacetResponse {

	@Getter
	@Setter
	private List<EstadoRegistroDTO> archivos;
	
}
