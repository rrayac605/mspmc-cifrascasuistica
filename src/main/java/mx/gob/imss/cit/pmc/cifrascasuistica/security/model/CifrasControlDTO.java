package mx.gob.imss.cit.pmc.cifrascasuistica.security.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class CifrasControlDTO extends FechasAuditoriaDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5855265134193383054L;
	/**
	 * 
	 */
	@Setter
	@Getter
	private Long numTotalRegistros;
	@Setter
	@Getter
	private Long numRegistrosCorrectos;
	@Setter
	@Getter
	private Long numRegistrosCorrectosOtras;
	@Setter
	@Getter
	private Long numRegistrosErrorOtras;
	@Setter
	@Getter
	private Long numRegistrosError;
	@Setter
	@Getter
	private Long numRegistrosSusOtras;
	@Setter
	@Getter
	private Long numRegistrosDupOtras;
	@Setter
	@Getter
	private Long numRegistrosBaja;
	@Setter
	@Getter
	private Long numRegistrosBajaOtras;
	@Setter
	@Getter
	private Long numRegistrosDup;
	@Setter
	@Getter
	private Long numRegistrosSus;

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
