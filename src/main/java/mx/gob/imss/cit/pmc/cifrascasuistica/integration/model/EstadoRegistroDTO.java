package mx.gob.imss.cit.pmc.cifrascasuistica.integration.model;

import lombok.Getter;
import lombok.Setter;

public class EstadoRegistroDTO {
	
@Getter
@Setter	
private String id;
	
@Getter
@Setter
private String name;

@Getter
@Setter
private int contador;

@Getter
@Setter
private String origenArchivo;

}
