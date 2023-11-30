package mx.gob.imss.cit.pmc.cifrascasuistica.services;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import net.sf.jasperreports.engine.JRException;

public interface ReporteService {

	Object getCifrasControlReport(MsPmcCifrasControlInput input) throws FileNotFoundException, JRException, IOException, BusinessException;

	Workbook getCifrasControlReportXls(MsPmcCifrasControlInput input)throws JRException, IOException, BusinessException;

}
