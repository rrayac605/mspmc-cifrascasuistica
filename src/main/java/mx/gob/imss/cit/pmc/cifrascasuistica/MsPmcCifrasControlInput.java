package mx.gob.imss.cit.pmc.cifrascasuistica;

import lombok.Data;

@Data
public class MsPmcCifrasControlInput {
	
	private String fromMonth;
	
	private String fromYear;
	
	private String toMonth;
	
	private String toYear;
	
	private String cveTipoArchivo;
	
	private String cveDelegation;
	
	private String cveSubdelegation;
	
	private String desDelegation;
	
	private String desSubdelegation;
	
	private Boolean delRegPat;
	
	private Boolean isPdfReport;
	
	private Integer page;
	
}
