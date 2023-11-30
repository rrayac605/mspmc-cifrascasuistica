package mx.gob.imss.cit.pmc.cifrascasuistica.services.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlMovimientosResponseDTO;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaDTO;

import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.DetalleSalidaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.security.model.CifrasControlDTO;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.MsPmcCifrasControlService;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.ReporteService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service("reportService")
public class ReporteServiceImpl implements ReporteService {

	public String exportReport(String reportFormat) throws FileNotFoundException, JRException {

		return "Report Generatade succefully";
	}

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private MsPmcCifrasControlService cifrasControlService;

	@Autowired
	private ParametroRepository parametroRepository;

	DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public Object getCifrasControlReport(MsPmcCifrasControlInput input)
			throws JRException, IOException, BusinessException {

		List<CifrasControlMovimientosResponseDTO> cifrasControlList = cifrasControlService.getCifrasControl(input);

		long total = 0L;
		long correctos = 0L;
		long erroneos = 0L;
		long duplicados = 0L;
		long suceptibles = 0L;
		long baja = 0L;
		long correctosOtras = 0L;
		long erroneosOtras = 0L;
		long duplicadosOtras = 0L;
		long suceptiblesOtras = 0L;
		long bajaOtras = 0L;

		for (CifrasControlMovimientosResponseDTO cifrasControl : cifrasControlList) {
			total = total + cifrasControl.getTotal();
			correctos = correctos + cifrasControl.getCorrecto();
			erroneos = erroneos + cifrasControl.getErroneo();
			duplicados = duplicados + cifrasControl.getDuplicado();
			suceptibles = suceptibles + cifrasControl.getSusceptible();
			baja = baja + cifrasControl.getBaja();
			correctosOtras = correctosOtras + cifrasControl.getCorrectoOtras();
			erroneosOtras = erroneosOtras + cifrasControl.getErroneoOtras();
			duplicadosOtras = duplicadosOtras + cifrasControl.getDuplicadoOtras();
			suceptiblesOtras = suceptiblesOtras + cifrasControl.getSusceptibleOtras();
			bajaOtras = bajaOtras + cifrasControl.getBajaOtrasDelegaciones();
		}

		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporteCasuistica");

		Map<String, Object> parameters = new HashMap<String, Object>();

		InputStream resourceAsStream=null;
		
			resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/cifrasControlNacional.jrxml");
			Optional<ParametroDTO> reporteNacional = parametroRepository.findOneByCve("reporteNacional");
			
			String tituloDelegacion="";
			if(input.getCveDelegation() == null || input.getCveDelegation().equals("") || input.getCveDelegation().equals("-1")) {
				tituloDelegacion="Nacional";
			}else{
				if(input.getDesDelegation() != null   &&  !input.getDesDelegation().equals("") && !input.getDesDelegation().equals("-1")){
					tituloDelegacion= input.getCveDelegation() + " " + input.getDesDelegation();
				}
				if(input.getDesSubdelegation() != null   &&  !input.getDesSubdelegation().equals("") && !input.getDesSubdelegation().equals("-1")){
					tituloDelegacion= tituloDelegacion + " / " + input.getCveSubdelegation() + " " + input.getDesSubdelegation();
				}
			
			}
			
			parameters.put("reporteDelegacional", tituloDelegacion);

		JasperReport jasperReport = JasperCompileManager.compileReport(resourceAsStream);

		parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
		parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
		parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
		parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
		parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
		parameters.put("nombreReporte", nombreReporte.get().getDesParametro());
		parameters.put("numTotalRegistros", total);
		parameters.put("numRegistrosCorrectos", correctos);
		parameters.put("numRegistrosCorrectosOtras", correctosOtras);
		parameters.put("numRegistrosErrorOtras", erroneosOtras);
		parameters.put("numRegistrosError", erroneos);
		parameters.put("numRegistrosSusOtras", suceptiblesOtras);
		parameters.put("numRegistrosDupOtras", duplicadosOtras);
		parameters.put("numRegistrosDup", duplicados);
		parameters.put("numRegistrosSus", suceptibles);

		//************************ BAJAS     *******************************************//
		parameters.put("numRegistrosBaja", baja);
		parameters.put("numRegistrosBajaOtras", bajaOtras);
		
		
		parameters.put("fromDate",
				DateUtils.calcularFechaPoceso(input.getFromMonth(), input.getFromYear()).format(europeanDateFormatter));
		parameters.put("toDate",
				DateUtils.calcularFechaPocesoFin(input.getToMonth(), input.getToYear()).format(europeanDateFormatter));
		if (cifrasControlList.size() > 1) {
			parameters.put("delegacion", "Varias");
		} else {
			parameters.put("delegacion", input.getDesDelegation());
		}

		parameters.put("cifrasDataSource", cifrasControlList);

		JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
		return Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
	}

	@Override
	public Workbook getCifrasControlReportXls(MsPmcCifrasControlInput input)
			throws JRException, IOException, BusinessException {
		List<CifrasControlMovimientosResponseDTO> cifrasControlList = cifrasControlService.getCifrasControl(input);
		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte");
		Optional<ParametroDTO> reporteNacional = parametroRepository.findOneByCve("reporteNacional");
		Workbook workbook = new XSSFWorkbook();
		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Montserrat");
		font.setFontHeightInPoints((short) 8);
		font.setBold(true);
		
		XSSFFont fontPeriodo = ((XSSFWorkbook) workbook).createFont();
		fontPeriodo.setFontName("Montserrat");
		fontPeriodo.setFontHeightInPoints((short) 8);
		fontPeriodo.setColor(HSSFColor.WHITE.index);
		fontPeriodo.setBold(true);
		CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle rowStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle periodReport = createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, false);
		CellStyle headerStyle = createStyle(font, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.BLACK.index, workbook, false);
		LocalDate localDate = LocalDate.now(ZoneId.of("America/Mexico_City"));
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
		Locale localeMexico = new Locale("es", "MX");
		InputStream inputStream = ReporteServiceImpl.class.getResourceAsStream("/IMSS-logo-.png");
		byte[] bytes = IOUtils.toByteArray(inputStream);
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		inputStream.close();
		// Returns an object that handles instantiating concrete classes
		CreationHelper helper = workbook.getCreationHelper();
		
		if (input.getDelRegPat()) {
			List<Month> months = processMonths(input);
			for (int i = 0; i < months.size(); i++) {
				Month fromMonth = months.get(i);
				input.setFromMonth(String.valueOf(fromMonth.getValue()));
				input.setToMonth(String.valueOf(fromMonth.getValue()));
				
					if (cifrasControlList!=null && !cifrasControlList.isEmpty()) {
						Sheet sheetDelegacional = workbook.createSheet("Delegacional " + fromMonth.getDisplayName(TextStyle.SHORT, localeMexico).toUpperCase());
						Header header = sheetDelegacional.getHeader();  
						header.setRight("Hoja " + HeaderFooter.page() + " de " + HeaderFooter.numPages()); 

						sheetDelegacional.setColumnWidth(0, 200);
						sheetDelegacional.setMargin(Sheet.LeftMargin, 0.5 /* inches */ );
						sheetDelegacional.setMargin(Sheet.RightMargin, 0.5 /* inches */ );
	
						Drawing drawing = sheetDelegacional.createDrawingPatriarch();

						ClientAnchor anchor = helper.createClientAnchor();
						// set top-left corner for the image
						anchor.setCol1(1);
						anchor.setRow1(1);

						// Creates a picture
						Picture pict = drawing.createPicture(anchor, pictureIdx);
						// Reset the image to the original size
						pict.resize(1.5, 5);
						
						CellRangeAddress region = CellRangeAddress.valueOf("B2:M7");
						for(int x=region.getFirstRow();x<region.getLastRow();x++){
						    Row row = sheetDelegacional.createRow(x);
						    for(int y=region.getFirstColumn();y<region.getLastColumn();y++){
						        Cell cell = row.createCell(y);
						        cell.setCellValue(" ");
						        cell.setCellStyle(headerStyle);
						    }
						}
						
						Row rowInstitucionDet = sheetDelegacional.getRow(1);
						
						Cell cellInstitucionDet = rowInstitucionDet.getCell(3);
						cellInstitucionDet.setCellValue(nombreInstitucion.get().getDesParametro());
						cellInstitucionDet.setCellStyle(headerStyle);
						
						Row rowDireccionDet = sheetDelegacional.getRow(2);
						Cell cellDireccionDet = rowDireccionDet.getCell(3);
						cellDireccionDet.setCellValue(direccionInstitucion.get().getDesParametro());
						cellDireccionDet.setCellStyle(headerStyle);
	
						Row rowUnidadDet = sheetDelegacional.getRow(3);
						Cell cellUnidadDet = rowUnidadDet.getCell(3);
						cellUnidadDet.setCellValue(unidadInstitucion.get().getDesParametro());
						cellUnidadDet.setCellStyle(headerStyle);
	
						Row rowCoordinacionDet = sheetDelegacional.getRow(4);
						Cell cellCoordinacionDet = rowCoordinacionDet.getCell(3);
						cellCoordinacionDet.setCellValue(coordinacionInstituc.get().getDesParametro());
						cellCoordinacionDet.setCellStyle(headerStyle);
	
						Row rowDivisionDet = sheetDelegacional.getRow(5);
						Cell cellDivisionDet = rowDivisionDet.getCell(3);
						cellDivisionDet.setCellValue(divisionInstitucion.get().getDesParametro());
						cellDivisionDet.setCellStyle(headerStyle);
	
						Cell cellFechaDet = rowDivisionDet.getCell(11);
						cellFechaDet.setCellValue(localDate.format(df));
						cellFechaDet.setCellStyle(headerStyle);
						
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D2:I2"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D3:J3"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D4:J4"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D5:J5"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D6:J6"));
	
						
						createHeaderReport(input, nombreReporte, reporteNacional, sheetDelegacional, true, font, fontPeriodo, workbook, fromMonth.getDisplayName(TextStyle.FULL, localeMexico).toUpperCase());
						
						int counterdet = createHeaderTable(cifrasControlList, sheetDelegacional, periodReport, font, workbook, input.getDesDelegation());
						
						fillDetailReport(cifrasControlList, sheetDelegacional, rowColorStyle, rowStyle, counterdet, true, font, workbook);
					}
				}
				
		} else {
			
			String sheetName="Nacional";


			Sheet sheetNacional = workbook.createSheet(sheetName);
			sheetNacional.setColumnWidth(0, 200);
			
			Header header = sheetNacional.getHeader();  
			header.setRight("Página " + HeaderFooter.page() + " of " + HeaderFooter.numPages()); 

			sheetNacional.setMargin(Sheet.LeftMargin, 0.5 /* inches */ );

			sheetNacional.setMargin(Sheet.RightMargin, 0.5 /* inches */ );

			// Creates the top-level drawing patriarch.
			Drawing drawing = sheetNacional.createDrawingPatriarch();

			ClientAnchor anchor = helper.createClientAnchor();
			// set top-left corner for the image
			anchor.setCol1(1);
			anchor.setRow1(1);

			// Creates a picture
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			// Reset the image to the original size
			pict.resize(1.5, 5);
			CellRangeAddress region = input.getDelRegPat()?CellRangeAddress.valueOf("B2:M7"):CellRangeAddress.valueOf("B2:L7");
			for(int i=region.getFirstRow();i<region.getLastRow();i++){
			    Row row = sheetNacional.createRow(i);
			    for(int j=region.getFirstColumn();j<region.getLastColumn();j++){
			        Cell cell = row.createCell(j);
			        cell.setCellValue(" ");
			        cell.setCellStyle(headerStyle);
			    }
			}

			Row rowInstitucion = sheetNacional.getRow(1);
			
			Cell cellInstitucion = rowInstitucion.getCell(3);
			cellInstitucion.setCellValue(nombreInstitucion.get().getDesParametro());
			cellInstitucion.setCellStyle(headerStyle);
			
			Row rowDireccion = sheetNacional.getRow(2);
			Cell cellDireccion = rowDireccion.getCell(3);
			cellDireccion.setCellValue(direccionInstitucion.get().getDesParametro());
			cellDireccion.setCellStyle(headerStyle);

			Row rowUnidad = sheetNacional.getRow(3);
			Cell cellUnidad = rowUnidad.getCell(3);
			cellUnidad.setCellValue(unidadInstitucion.get().getDesParametro());
			cellUnidad.setCellStyle(headerStyle);

			Row rowCoordinacion = sheetNacional.getRow(4);
			Cell cellCoordinacion = rowCoordinacion.getCell(3);
			cellCoordinacion.setCellValue(coordinacionInstituc.get().getDesParametro());
			cellCoordinacion.setCellStyle(headerStyle);

			Row rowDivision = sheetNacional.getRow(5);
			Cell cellDivision = rowDivision.getCell(3);
			cellDivision.setCellValue(divisionInstitucion.get().getDesParametro());
			cellDivision.setCellStyle(headerStyle);

			Cell cellFecha = rowDivision.getCell(10);
			cellFecha.setCellValue(localDate.format(df));
			cellFecha.setCellStyle(headerStyle);
			
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D2:I2"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D3:J3"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D4:J4"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D5:J5"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D6:J6"));

			createHeaderReport(input, nombreReporte, reporteNacional, sheetNacional, false, font, fontPeriodo, workbook, null);
			
			int counter = createHeaderTable(cifrasControlList, sheetNacional, rowColorStyle, font, workbook, input.getDesDelegation());
			
			fillDetailReport(cifrasControlList, sheetNacional, rowColorStyle, rowStyle, counter, false, font, workbook);
		}
		
		return workbook;
	}


	private List<Month> processMonths(MsPmcCifrasControlInput input) {
		LocalDate fecProcesoIni = DateUtils.calcularFecPoceso(input.getFromMonth(), input.getFromYear());
		LocalDate fecProcesoFin = DateUtils.calcularFecPocesoFin(input.getToMonth(), input.getToYear());
		List<Month> months = new ArrayList<Month>();
		int initialMonth = fecProcesoIni.getMonthValue();
		int finalMonth = fecProcesoFin.getMonthValue();
		for (int i = initialMonth; i <= finalMonth; i++) {
			months.add(Month.of(i));
		}
		
		
		return months;
	}

	private void fillDetailReport(List<CifrasControlMovimientosResponseDTO> cifrasControlDTO, Sheet sheetNacional, CellStyle centerStyleCell,
			CellStyle wrapStyle, int counter, boolean delRegaPat, XSSFFont font, Workbook workbook) {
		Row rowTotal = sheetNacional.createRow(counter+14);
		
		CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle rowtyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
		
		CellStyle style = null;
		if ((counter+14) % 2 == 0) {
			style=rowtyle;
		} else {
			style=rowColorStyle;
		}
		
		Cell tipoArchivoDetCell = rowTotal.createCell(1);
		tipoArchivoDetCell.setCellValue("Totales");
		tipoArchivoDetCell.setCellStyle(style);

		long total = 0L;
		long correctos = 0L;
		long erroneos = 0L;
		long duplicados = 0L;
		long suceptibles = 0L;
		long baja = 0L;
		long correctosOtras = 0L;
		long erroneosOtras = 0L;
		long duplicadosOtras = 0L;
		long suceptiblesOtras = 0L;
		long bajaOtras = 0L;

		for (CifrasControlMovimientosResponseDTO cifrasControl : cifrasControlDTO) {
			total = total + cifrasControl.getTotal();
			correctos = correctos + cifrasControl.getCorrecto();
			erroneos = erroneos + cifrasControl.getErroneo();
			duplicados = duplicados + cifrasControl.getDuplicado();
			suceptibles = suceptibles + cifrasControl.getSusceptible();
			baja = baja + cifrasControl.getBaja();
			correctosOtras = correctosOtras + cifrasControl.getCorrectoOtras();
			erroneosOtras = erroneosOtras + cifrasControl.getErroneoOtras();
			duplicadosOtras = duplicadosOtras + cifrasControl.getDuplicadoOtras();
			suceptiblesOtras = suceptiblesOtras + cifrasControl.getSusceptibleOtras();
			bajaOtras = bajaOtras + cifrasControl.getBajaOtrasDelegaciones();
		}

		int i = 0;

		if (delRegaPat) {
			Cell delegacionDetCell = rowTotal.createCell(2);
			delegacionDetCell.setCellValue("");
			delegacionDetCell.setCellStyle(style);
			i = 1;
		}
			
		Cell totalDetCell = rowTotal.createCell(2 + i);
		totalDetCell.setCellValue(total);
		totalDetCell.setCellStyle(style);

		Cell correctosDetCell = rowTotal.createCell(3 + i);
		correctosDetCell.setCellValue(correctos);
		correctosDetCell.setCellStyle(style);

		Cell erroneosDetCell = rowTotal.createCell(4 + i);
		erroneosDetCell.setCellValue(erroneos);
		erroneosDetCell.setCellStyle(style);

		Cell duplicadosDetCell = rowTotal.createCell(5 + i);
		duplicadosDetCell.setCellValue(duplicados);
		duplicadosDetCell.setCellStyle(style);

		Cell susAjusDetCell = rowTotal.createCell(6 + i);
		susAjusDetCell.setCellValue(suceptibles);
		susAjusDetCell.setCellStyle(style);

		Cell correctosOtrasDetCell = rowTotal.createCell(7 + i);
		correctosOtrasDetCell.setCellValue(correctosOtras);
		correctosOtrasDetCell.setCellStyle(style);

		Cell erroneosOtrasDetCell = rowTotal.createCell(8 + i);
		erroneosOtrasDetCell.setCellValue(erroneosOtras);
		erroneosOtrasDetCell.setCellStyle(style);

		Cell duplicadosOtrasDetCell = rowTotal.createCell(9 + i);
		duplicadosOtrasDetCell.setCellValue(duplicadosOtras);
		duplicadosOtrasDetCell.setCellStyle(style);

		Cell susAjusOtrasDetCell = rowTotal.createCell(10 + i);
		susAjusOtrasDetCell.setCellValue(suceptiblesOtras);
		susAjusOtrasDetCell.setCellStyle(style);
	}

	private int createHeaderTable(List<CifrasControlMovimientosResponseDTO> detalleConsultaDTOList, Sheet sheetNacional,
								  CellStyle rowColorStyle2, XSSFFont font, Workbook workbook, String desDelegacion) {
		int counter = 1;
		for (int i = 0; i < detalleConsultaDTOList.size(); i++) {
			CifrasControlMovimientosResponseDTO detalleConsultaDTO = detalleConsultaDTOList.get(i);
			counter++;
			Row rowDetail = sheetNacional.createRow(i+15);
			
			CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
			CellStyle rowtyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
			
			CellStyle style = null;
			if (i % 2 == 0) {
				style=rowColorStyle;
			} else {
				style=rowtyle;
			}
			
			
			Cell tipoArchivoDetCell = rowDetail.createCell(1);
			tipoArchivoDetCell.setCellValue(detalleConsultaDTO.get_id());
			tipoArchivoDetCell.setCellStyle(style);
			
			if (desDelegacion!=null) {
				
				Cell delegacionCell = rowDetail.createCell(2);
				delegacionCell.setCellValue(desDelegacion);
				delegacionCell.setCellStyle(style);
				
				Cell totalDetCell = rowDetail.createCell(3);
				totalDetCell.setCellValue(detalleConsultaDTO.getTotal());
				totalDetCell.setCellStyle(style);

				Cell correctosDetCell = rowDetail.createCell(4);
				correctosDetCell.setCellValue(detalleConsultaDTO.getCorrecto()!=null?detalleConsultaDTO.getCorrecto():0);
				correctosDetCell.setCellStyle(style);

				Cell erroneosDetCell = rowDetail.createCell(5);
				erroneosDetCell.setCellValue(detalleConsultaDTO.getErroneo()!=null?detalleConsultaDTO.getErroneo():0);
				erroneosDetCell.setCellStyle(style);

				Cell duplicadosDetCell = rowDetail.createCell(6);
				duplicadosDetCell.setCellValue(detalleConsultaDTO.getDuplicado()!=null?detalleConsultaDTO.getDuplicado():0);
				duplicadosDetCell.setCellStyle(style);

				Cell susAjusDetCell = rowDetail.createCell(7);
				susAjusDetCell.setCellValue(detalleConsultaDTO.getSusceptible()!=null?detalleConsultaDTO.getSusceptible():0);
				susAjusDetCell.setCellStyle(style);

				Cell correctosOtrasDetCell = rowDetail.createCell(8);
				correctosOtrasDetCell.setCellValue(detalleConsultaDTO.getCorrectoOtras()!=null?detalleConsultaDTO.getCorrectoOtras():0);
				correctosOtrasDetCell.setCellStyle(style);

				Cell erroneosOtrasDetCell = rowDetail.createCell(9);
				erroneosOtrasDetCell.setCellValue(detalleConsultaDTO.getErroneoOtras()!=null?detalleConsultaDTO.getErroneoOtras():0);
				erroneosOtrasDetCell.setCellStyle(style);

				Cell duplicadosOtrasDetCell = rowDetail.createCell(10);
				duplicadosOtrasDetCell.setCellValue(detalleConsultaDTO.getDuplicadoOtras()!=null?detalleConsultaDTO.getDuplicadoOtras():0);
				duplicadosOtrasDetCell.setCellStyle(style);

				Cell susAjusOtrasDetCell = rowDetail.createCell(11);
				susAjusOtrasDetCell.setCellValue(detalleConsultaDTO.getSusceptibleOtras()!=null?detalleConsultaDTO.getSusceptibleOtras():0);
				susAjusOtrasDetCell.setCellStyle(style);
			} else {
				Cell totalDetCell = rowDetail.createCell(2);
				totalDetCell.setCellValue(detalleConsultaDTO.getTotal());
				totalDetCell.setCellStyle(style);

				Cell correctosDetCell = rowDetail.createCell(3);
				correctosDetCell.setCellValue(detalleConsultaDTO.getCorrecto()!=null?detalleConsultaDTO.getCorrecto():0);
				correctosDetCell.setCellStyle(style);

				Cell erroneosDetCell = rowDetail.createCell(4);
				erroneosDetCell.setCellValue(detalleConsultaDTO.getErroneo()!=null?detalleConsultaDTO.getErroneo():0);
				erroneosDetCell.setCellStyle(style);

				Cell duplicadosDetCell = rowDetail.createCell(5);
				duplicadosDetCell.setCellValue(detalleConsultaDTO.getDuplicado()!=null?detalleConsultaDTO.getDuplicado():0);
				duplicadosDetCell.setCellStyle(style);

				Cell susAjusDetCell = rowDetail.createCell(6);
				susAjusDetCell.setCellValue(detalleConsultaDTO.getSusceptible()!=null?detalleConsultaDTO.getSusceptible():0);
				susAjusDetCell.setCellStyle(style);

				Cell correctosOtrasDetCell = rowDetail.createCell(7);
				correctosOtrasDetCell.setCellValue(detalleConsultaDTO.getCorrectoOtras()!=null?detalleConsultaDTO.getCorrectoOtras():0);
				correctosOtrasDetCell.setCellStyle(style);

				Cell erroneosOtrasDetCell = rowDetail.createCell(8);
				erroneosOtrasDetCell.setCellValue(detalleConsultaDTO.getErroneoOtras()!=null?detalleConsultaDTO.getErroneoOtras():0);
				erroneosOtrasDetCell.setCellStyle(style);

				Cell duplicadosOtrasDetCell = rowDetail.createCell(9);
				duplicadosOtrasDetCell.setCellValue(detalleConsultaDTO.getDuplicadoOtras()!=null?detalleConsultaDTO.getDuplicadoOtras():0);
				duplicadosOtrasDetCell.setCellStyle(style);

				Cell susAjusOtrasDetCell = rowDetail.createCell(10);
				susAjusOtrasDetCell.setCellValue(detalleConsultaDTO.getSusceptibleOtras()!=null?detalleConsultaDTO.getSusceptibleOtras():0);
				susAjusOtrasDetCell.setCellStyle(style);
			}
			
			
		}
		return counter;
	}

	private void createHeaderReport(MsPmcCifrasControlInput input, Optional<ParametroDTO> nombreReporte,
			Optional<ParametroDTO> reporteNacional, Sheet sheetNacional, boolean b, XSSFFont font, XSSFFont fontPeriodo, Workbook workbook, String monthName) {
		Row rowNombreReporte = sheetNacional.createRow(7);
		Cell nombreReporteCell = rowNombreReporte.createCell(1);
		nombreReporteCell.setCellValue(nombreReporte.get().getDesParametro());
		nombreReporteCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat()) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B8:L8"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B8:K8"));
		}

		Row rowReporteNacional = sheetNacional.createRow(8);
		Cell reporteNacionalCell = rowReporteNacional.createCell(1);
		reporteNacionalCell.setCellValue(reporteNacional.get().getDesParametro());
		reporteNacionalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat()) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B9:L9"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B9:K9"));
		}

		Row rowPeriodoConsultado = sheetNacional.createRow(10);
		Cell periodoConsultado = rowPeriodoConsultado.createCell(1);
		periodoConsultado.setCellValue("Periodo consultado: ");
		periodoConsultado.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B11:C11"));

		Cell fecInicio = rowPeriodoConsultado.createCell(3);
		fecInicio.setCellValue(
				DateUtils.calcularFechaPoceso(input.getFromMonth(), input.getFromYear()).format(europeanDateFormatter));
		fecInicio.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Cell al = rowPeriodoConsultado.createCell(4);
		al.setCellValue(" al ");
		al.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Cell fecFin = rowPeriodoConsultado.createCell(5);
		fecFin.setCellValue(
				DateUtils.calcularFechaPocesoFin(input.getToMonth(), input.getToYear()).format(europeanDateFormatter));
		fecFin.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Row rowRegistros = sheetNacional.createRow(12);
		Cell registrosCell = rowRegistros.createCell(3);
		registrosCell.setCellValue("Registros");
		registrosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B13:C14"));
		
		Cell mesConsultado = rowRegistros.createCell(1);
		mesConsultado.setCellValue(monthName);
		mesConsultado.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		
		if (input.getDelRegPat() && b) {
			Cell registrosCellOtras = rowRegistros.createCell(8);
			registrosCellOtras.setCellValue("Registros");
			registrosCellOtras.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			Cell registrosCellOtras = rowRegistros.createCell(7);
			registrosCellOtras.setCellValue("Registros");
			registrosCellOtras.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		}
		
		
		//Naional
		if (input.getDelRegPat() && b) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D13:H13"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("I13:L13"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D13:G13"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("H13:K13"));
		}

		Row rowDelegacion = sheetNacional.createRow(13);
		Cell delegacionCell = rowDelegacion.createCell(3);
		delegacionCell.setCellValue("Delegación");
		delegacionCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat() && b) {
			Cell delegacionOtrasCell = rowDelegacion.createCell(8);
			delegacionOtrasCell.setCellValue("Otras Delegaciones");
			delegacionOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			Cell delegacionOtrasCell = rowDelegacion.createCell(7);
			delegacionOtrasCell.setCellValue("Otras Delegaciones");
			delegacionOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		}
		if (input.getDelRegPat() && b) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D14:H14"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("I14:L14"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D14:G14"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("H14:K14"));
		}

		Row rowEncabezadoNacional = sheetNacional.createRow(14);
		Cell tipoArchivoCell = rowEncabezadoNacional.createCell(1);
		tipoArchivoCell.setCellValue("Tipo Archivo");
		tipoArchivoCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		if (input.getDelRegPat()) {
			
			Cell delDetCell = rowEncabezadoNacional.createCell(2);
			delDetCell.setCellValue("Delegacion");
			delDetCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			
			Cell totalCell = rowEncabezadoNacional.createCell(3);
			totalCell.setCellValue("Total");
			totalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			Cell correctosCell = rowEncabezadoNacional.createCell(4);
			correctosCell.setCellValue("Correctos");
			correctosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosCell = rowEncabezadoNacional.createCell(5);
			erroneosCell.setCellValue("Erróneos");
			erroneosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosCell = rowEncabezadoNacional.createCell(6);
			duplicadosCell.setCellValue("Duplicados");
			duplicadosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusCell = rowEncabezadoNacional.createCell(7);
			susAjusCell.setCellValue("Susceptibles de Ajuste");
			susAjusCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell correctosOtrasCell = rowEncabezadoNacional.createCell(8);
			correctosOtrasCell.setCellValue("Correctos");
			correctosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosOtrasCell = rowEncabezadoNacional.createCell(9);
			erroneosOtrasCell.setCellValue("Erróneos");
			erroneosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosOtrasCell = rowEncabezadoNacional.createCell(10);
			duplicadosOtrasCell.setCellValue("Duplicados");
			duplicadosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusOtrasCell = rowEncabezadoNacional.createCell(11);
			susAjusOtrasCell.setCellValue("Susceptibles de Ajuste");
			susAjusOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			
			Cell totalCell = rowEncabezadoNacional.createCell(2);
			totalCell.setCellValue("Total");
			totalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			Cell correctosCell = rowEncabezadoNacional.createCell(3);
			correctosCell.setCellValue("Correctos");
			correctosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosCell = rowEncabezadoNacional.createCell(4);
			erroneosCell.setCellValue("Erróneos");
			erroneosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosCell = rowEncabezadoNacional.createCell(5);
			duplicadosCell.setCellValue("Duplicados");
			duplicadosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusCell = rowEncabezadoNacional.createCell(6);
			susAjusCell.setCellValue("Susceptibles de Ajuste");
			susAjusCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell correctosOtrasCell = rowEncabezadoNacional.createCell(7);
			correctosOtrasCell.setCellValue("Correctos");
			correctosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosOtrasCell = rowEncabezadoNacional.createCell(8);
			erroneosOtrasCell.setCellValue("Erróneos");
			erroneosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosOtrasCell = rowEncabezadoNacional.createCell(9);
			duplicadosOtrasCell.setCellValue("Duplicados");
			duplicadosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusOtrasCell = rowEncabezadoNacional.createCell(10);
			susAjusOtrasCell.setCellValue("Susceptibles de Ajuste");
			susAjusOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
		}
	}
	
	private CellStyle createStyle(XSSFFont font, HorizontalAlignment hAlign, VerticalAlignment vAlign,  short cellColor, boolean cellBorder, short cellBorderColor,Workbook workbook, boolean wrap) {
		 
		CellStyle style = workbook.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(cellColor);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(hAlign);
		style.setVerticalAlignment(vAlign);
		style.setWrapText(wrap);
		
 
		if (cellBorder) {
			style.setBorderTop(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderBottom(BorderStyle.THIN);
 
			style.setTopBorderColor(cellBorderColor);
			style.setLeftBorderColor(cellBorderColor);
			style.setRightBorderColor(cellBorderColor);
			style.setBottomBorderColor(cellBorderColor);
		}
 
		return style;
	}
	

}

