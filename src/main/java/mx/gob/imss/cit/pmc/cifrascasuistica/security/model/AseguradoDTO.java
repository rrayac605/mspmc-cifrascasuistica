package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

public class AseguradoDTO extends FechasAuditoriaDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@Setter
	@Getter
	private String numNss;
	@Setter
	@Getter
	private String refCurp;
	@Setter
	@Getter
	private String nomAsegurado;
	@Setter
	@Getter
	private BigDecimal numSalarioDiario;
	@Setter
	@Getter
	private String numCicloAnual;
	@Setter
	@Getter
	private String refFolioOriginal;
	@Setter
	@Getter
	private Integer cveCasoRegistro;
	@Setter
	@Getter
	private String cveCodigoError;
	@Setter
	@Getter
	private Integer cveDelegacionAtencion;
	@Setter
	@Getter
	private Integer cveDelegacionNss;
	@Setter
	@Getter
	private Integer cveEstadoRegistro;
	@Setter
	@Getter
	private Integer cveSubDelAtencion;
	@Setter
	@Getter
	private Integer cveSubdelNss;
	@Setter
	@Getter
	private Integer cveUmfAdscripcion;
	@Setter
	@Getter
	private Integer cveUmfExp;
	@Setter
	@Getter
	private Integer cveUmfPagadora;
	@Setter
	@Getter
	private String cveOcupacion;
	@Setter
	@Getter
	private String objectIdArchivo;
	@Setter
	@Getter
	private String desCasoRegistro;
	@Setter
	@Getter
	private String desCodigoError;
	@Setter
	@Getter
	private String desDelegacionAtencion;
	@Setter
	@Getter
	private String desDelegacionNss;
	@Setter
	@Getter
	private String desEstadoRegistro;
	@Setter
	@Getter
	private String desOcupacion;
	@Setter
	@Getter
	private String desSubDelAtencion;
	@Setter
	@Getter
	private String desSubDelNss;
	@Setter
	@Getter
	private String desUmfAdscripcion;
	@Setter
	@Getter
	private String desUmfExp;
	@Setter
	@Getter
	private String desUmfPagadora;
	@Setter
	@Getter
	private String refPrimerApellido;
	@Setter
	@Getter
	private String refSegundoApellido;
	@Setter
	@Getter
	private Integer cveIdPersona;
	@Setter
	@Getter
	private Integer numIndice;


}
