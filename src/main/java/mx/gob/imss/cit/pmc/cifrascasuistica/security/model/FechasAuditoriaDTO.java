package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.lang.Nullable;

import lombok.Getter;
import lombok.Setter;
import mx.gob.imss.cit.pmc.support.CustomDateDeserializer;
import mx.gob.imss.cit.pmc.support.CustomDateSerializer;

public class FechasAuditoriaDTO {

	@Setter
	@Getter
	@Nullable
	@JsonDeserialize( using = CustomDateDeserializer.class )
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date fecAlta;
	@Setter
	@Getter
	@Nullable
	@JsonDeserialize( using = CustomDateDeserializer.class )
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date fecBaja;
	@Setter
	@Getter
	@Nullable
	@JsonDeserialize( using = CustomDateDeserializer.class )
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date fecActualizacion;

}
