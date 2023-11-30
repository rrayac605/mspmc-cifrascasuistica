package mx.gob.imss.cit.pmc.cifrascasuistica.services;

import java.io.FileNotFoundException;
import java.io.IOException;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import net.sf.jasperreports.engine.JRException;

public interface ReporteMovimientosCICService {
	
	Object getMovimientosCICReport(MsPmcCifrasControlInput input) throws FileNotFoundException, JRException, IOException, BusinessException;

}
