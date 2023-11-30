package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "MCT_ARCHIVO")
@JsonPropertyOrder({ "objectIdArchivo", "objectIdArchivo" })
public class ArchivoDTO extends FechasAuditoriaDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@Id
	private String objectIdArchivo;

	@Getter
	@Setter
	private String desIdArchivo;

	@Getter
	@Setter
	private String nomArchivo;

	@Getter
	@Setter
	private Date fecProcesoCarga;

	@Getter
	@Setter
	private String desArchivo;

	@Getter
	@Setter
	private String desError;

	@Getter
	@Setter
	private String numTotalRegistros;
	
	@Getter
	@Setter
	private String cveEstadoArchivo;

	@Getter
	@Setter
	private String desEstadoArchivo;

	@Getter
	@Setter
	private String cveOrigenArchivo;

	@Getter
	@Setter
	private String desOrigenArchivo;

	@Getter
	@Setter
	private CifrasControlDTO cifrasControlDTO;

	@Getter
	@Setter
	private List<DetalleRegistroDTO> detalleRegistroDTO;

}
