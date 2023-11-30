package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;
import mx.gob.imss.cit.pmc.support.CustomDateDeserializer;
import mx.gob.imss.cit.pmc.support.CustomDateSerializer;

@Document("arDetalle")
public class DetalleRegistroDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@JsonDeserialize( using = CustomDateDeserializer.class )
	@JsonSerialize(using = CustomDateSerializer.class)
	Date fecProcesoCarga;
	
	@Getter
	@Setter
	private String ObjectIdOrigen;
	
	@Getter
	@Setter
	private AseguradoDTO aseguradoDTO;

	@Getter
	@Setter
	private PatronDTO patronDTO;

	@Getter
	@Setter
	private IncapacidadDTO incapacidadDTO;
	
	@Setter
	@Getter
	private List<BitacoraErroresDTO> bitacoraErroresDTO;

}
