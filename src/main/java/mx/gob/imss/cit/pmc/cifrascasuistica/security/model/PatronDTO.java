package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.lang.Nullable;

import lombok.Getter;
import lombok.Setter;

@Document
public class PatronDTO extends FechasAuditoriaDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@Setter
	@Getter
	@Field
	@Nullable
	private String refRegistroPatronal;
	@Setter
	@Getter
	@Nullable
	private String desRazonSocial;
	@Setter
	@Getter
	@Nullable
	private String desRfc;
	@Setter
	@Getter
	@Nullable
	private String objectIdPatron;
	@Setter
	@Getter
	@Nullable
	private Integer cveClase;
	@Setter
	@Getter
	@Nullable
	private Integer cveDelRegPatronal;
	@Setter
	@Getter
	@Nullable
	private Integer cveFraccion;
	@Setter
	@Getter
	@Nullable
	private BigDecimal numPrima;
	@Setter
	@Getter
	@Nullable
	private Integer cveSubDelRegPatronal;
	@Setter
	@Getter	
	private String desClase;
	@Setter
	@Getter
	@Nullable
	private String desDelRegPatronal;
	@Setter
	@Getter
	@Nullable
	private String desFraccion;
	@Setter
	@Getter
	@Nullable
	private String desPrima;
	@Setter
	@Getter
	@Nullable
	private String desSubDelRegPatronal;
	
	@Setter
	@Getter	
	private String cveDelegacionAux;
	
	@Setter
	@Getter
	private String cveModalidad;

}
