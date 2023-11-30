package mx.gob.imss.cit.pmc.cifrascasuistica.integration.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false)
public class CifrasControlDTO extends FechasAuditoriaDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5855265134193383054L;
	/**
	 * 
	 */
	private Long numTotalRegistros;
	private Long numRegistrosCorrectos;
	private Long numRegistrosCorrectosOtras;
	private Long numRegistrosErrorOtras;
	private Long numRegistrosError;
	private Long numRegistrosSusOtras;
	private Long numRegistrosDupOtras;
	private Long numRegistrosBaja;
	private Long numRegistrosBajaOtras;
	private Long numRegistrosDup;
	private Long numRegistrosSus;
	private Long numTotalElementsPage;
	private Long numTotalPages;
	
	@Override
	public String toString() {
		return "CifrasControlDTO [numTotalRegistros=" + numTotalRegistros + ", numRegistrosCorrectos="
				+ numRegistrosCorrectos + ", numRegistrosCorrectosOtras=" + numRegistrosCorrectosOtras
				+ ", numRegistrosErrorOtras=" + numRegistrosErrorOtras + ", numRegistrosError=" + numRegistrosError
				+ ", numRegistrosSusOtras=" + numRegistrosSusOtras + ", numRegistrosDupOtras=" + numRegistrosDupOtras
				+ ", numRegistrosBaja=" + numRegistrosBaja + ", numRegistrosBajaOtras=" + numRegistrosBajaOtras
				+ ", numRegistrosDup=" + numRegistrosDup + ", numRegistrosSus=" + numRegistrosSus + "]";
	}

}
