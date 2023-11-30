package mx.gob.imss.cit.pmc.cifrascasuistica.services.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.util.StringUtils;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleMovimientoCasuisticaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.MsPmcCifrasControlService;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.ReporteMovimientosCICService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service("reporteMovimientosCICService")
public class ReporteMovimientosCICServiceImpl implements ReporteMovimientosCICService {

	@Autowired
	private MsPmcCifrasControlService cifrasControlService;

	@Autowired
	private ParametroRepository parametroRepository;

	DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public Object getMovimientosCICReport(MsPmcCifrasControlInput input)
			throws FileNotFoundException, JRException, IOException, BusinessException {
		String alcanceReporte = "";
		String alcance = "";
		
		if ( StringUtils.isNotBlank(input.getCveTipoArchivo()) && input.getCveTipoArchivo().equals("General") ) {
			input.setCveTipoArchivo(" ");
		}
		
		MovimientoCasuisticaOutput movimientosCIC = cifrasControlService.getMovimientoCasuistica(input);

		List<DetalleMovimientoCasuisticaDTO> detalleConsultaDTO = movimientosCIC.getDetalleConsultaDTO();
		DetalleMovimientoCasuisticaDTO totalesCasuisticaDTO = movimientosCIC.getTotalesCasuisticaDTO();

		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion_CIC");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion_CIC");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion_CIC");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion_CIC");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte_CIC");

		Map<String, Object> parameters = new HashMap<String, Object>();

		InputStream resourceAsStream = null;
			resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/movimientosCIC.jrxml");

		JasperReport jasperReport = JasperCompileManager.compileReport(resourceAsStream);

		parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
		parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
		parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
		parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
		parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
		parameters.put("nombreReporte", nombreReporte.get().getDesParametro());
		
		if (StringUtils.isNotEmpty(input.getCveDelegation()) && StringUtils.isNotEmpty(input.getCveSubdelegation())) {
			alcanceReporte = "Subdelegacional";
			alcance = input.getCveDelegation().concat(" ").concat(input.getDesDelegation()) +
					  " / ".concat(input.getCveSubdelegation()).concat(" ").concat(input.getDesSubdelegation());
		}else if (StringUtils.isNotEmpty(input.getCveDelegation()) && StringUtils.isEmpty(input.getCveSubdelegation())) {
			alcanceReporte = "Delegacional";
			alcance = input.getCveDelegation().concat(" ").concat(input.getDesDelegation());
		}else if (StringUtils.isEmpty(input.getCveDelegation()) && StringUtils.isEmpty(input.getCveSubdelegation())) {
			alcanceReporte = alcance = "Nacional";
		}

		parameters.put("alcanceReporte", alcanceReporte);
		parameters.put("alcance", alcance);
		parameters.put("tipoArchivo", input.getCveTipoArchivo());
		
		parameters.put("numTotalRegistros", totalesCasuisticaDTO.getNumTotalRegistros());
		parameters.put("numTotalRegistrosOtrDel", totalesCasuisticaDTO.getNumTotalRegistrosOtrDel());
		parameters.put("numRegistrosCorrectos", totalesCasuisticaDTO.getNumRegistrosCorrectos());
		parameters.put("numRegistrosCorrectosOtras", totalesCasuisticaDTO.getNumRegistrosCorrectosOtrDel());
		parameters.put("numRegistrosErrorOtras", totalesCasuisticaDTO.getNumRegistrosErroneosOtrDel());
		parameters.put("numRegistrosError", totalesCasuisticaDTO.getNumRegistrosErroneos());
		parameters.put("numRegistrosSusOtras", totalesCasuisticaDTO.getNumRegistrosSusAjusteOtrDel());
		parameters.put("numRegistrosSus", totalesCasuisticaDTO.getNumRegistrosSusAjuste());
		parameters.put("numRegistrosDupOtras", totalesCasuisticaDTO.getNumRegistrosDuplicadosOtrDel());
		parameters.put("numRegistrosDup", totalesCasuisticaDTO.getNumRegistrosDuplicados());
		parameters.put("numTotalRegistrosGeneral", totalesCasuisticaDTO.getNumTotalGenerales());

		
		
		parameters.put("fromDate",
				DateUtils.calcularFechaPoceso(input.getFromMonth(), input.getFromYear()).format(europeanDateFormatter));
		parameters.put("toDate",
				DateUtils.calcularFechaPocesoFin(input.getToMonth(), input.getToYear()).format(europeanDateFormatter));

		parameters.put("cifrasMovimientosCIC", detalleConsultaDTO);

		JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
		JasperExportManager.exportReportToPdfFile(print, "C:\\dev\\reports\\CifrasMovimientoCIC.pdf");
		return Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
	}

}
