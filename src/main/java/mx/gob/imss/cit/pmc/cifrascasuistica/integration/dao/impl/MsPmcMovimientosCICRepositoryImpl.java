package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.CountOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import io.micrometer.core.instrument.util.StringUtils;
import mx.gob.imss.cit.mspmccommons.enums.IdentificadorArchivoEnum;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.CambioDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.CountDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleMovimientoCasuisticaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.MsPmcMovimientosCICRepository;

@Repository
public class MsPmcMovimientosCICRepositoryImpl implements MsPmcMovimientosCICRepository {

	@Autowired
	private MongoOperations mongoOperations;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public MovimientoCasuisticaOutput getMovimientosCasuistica(MsPmcCifrasControlInput input) throws BusinessException {
		List<DetalleMovimientoCasuisticaDTO> detalleOutput = new ArrayList<DetalleMovimientoCasuisticaDTO>();

		// Se calculan las fechas inicio y fin para la consulta
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		logger.info("MsPmcMovimientosCICRepositoryImpl:getMovimientosCasuistica:fecProcesoInicio: " + fecProcesoIni);
		logger.info("MsPmcMovimientosCICRepositoryImpl:getMovimientosCasuistica:fecProcesoFin: " + fecProcesoFin);
		logger.info("cveDelegacion recibida: " + input.getCveDelegation());
		logger.info("cveSubDelegacion recibida: " + input.getCveSubdelegation());
		logger.info("CveTipoEntrada recibida: " + input.getCveTipoArchivo());

		DetalleMovimientoCasuisticaDTO alta = calcularAlta(input, fecProcesoIni, fecProcesoFin);
		DetalleMovimientoCasuisticaDTO baja = calcularBaja(input, fecProcesoIni, fecProcesoFin);
		DetalleMovimientoCasuisticaDTO modificacion = calcularModificado(input, fecProcesoIni, fecProcesoFin);
		DetalleMovimientoCasuisticaDTO pendAprob = calcularPendienteAprobar(input, fecProcesoIni, fecProcesoFin);
		DetalleMovimientoCasuisticaDTO rechazados = calcularRechazados(input, fecProcesoIni, fecProcesoFin);
		DetalleMovimientoCasuisticaDTO aprobados = calcularAprobados(input, fecProcesoIni, fecProcesoFin);

		detalleOutput.add(alta);
		detalleOutput.add(baja);
		detalleOutput.add(modificacion);
		detalleOutput.add(pendAprob);
		detalleOutput.add(rechazados);
		detalleOutput.add(aprobados);

		DetalleMovimientoCasuisticaDTO totalesCasuisticaDTO = calcularTotales(alta, baja, modificacion, pendAprob,
				rechazados, aprobados);
		MovimientoCasuisticaOutput resultPage = new MovimientoCasuisticaOutput();
		resultPage.setDetalleConsultaDTO(detalleOutput);
		resultPage.setTotalesCasuisticaDTO(totalesCasuisticaDTO);

		return resultPage;
	}

	private DetalleMovimientoCasuisticaDTO calcularTotales(DetalleMovimientoCasuisticaDTO alta,
			DetalleMovimientoCasuisticaDTO baja, DetalleMovimientoCasuisticaDTO modificacion,
			DetalleMovimientoCasuisticaDTO pendAprob, DetalleMovimientoCasuisticaDTO rechazados,
			DetalleMovimientoCasuisticaDTO aprobados) {
		DetalleMovimientoCasuisticaDTO totales = new DetalleMovimientoCasuisticaDTO();

		totales.setTipoEntrada("Totales");
		totales.setNumRegistrosCorrectos(alta.getNumRegistrosCorrectos() + baja.getNumRegistrosCorrectos()
				+ modificacion.getNumRegistrosCorrectos() + pendAprob.getNumRegistrosCorrectos()
				+ rechazados.getNumRegistrosCorrectos() + aprobados.getNumRegistrosCorrectos());
		totales.setNumRegistrosErroneos(alta.getNumRegistrosErroneos() + baja.getNumRegistrosErroneos()
				+ modificacion.getNumRegistrosErroneos() + pendAprob.getNumRegistrosErroneos()
				+ rechazados.getNumRegistrosErroneos() + aprobados.getNumRegistrosErroneos());
		totales.setNumRegistrosDuplicados(alta.getNumRegistrosDuplicados() + baja.getNumRegistrosDuplicados()
				+ modificacion.getNumRegistrosDuplicados() + pendAprob.getNumRegistrosDuplicados()
				+ rechazados.getNumRegistrosDuplicados() + aprobados.getNumRegistrosDuplicados());
		totales.setNumRegistrosSusAjuste(alta.getNumRegistrosSusAjuste() + baja.getNumRegistrosSusAjuste()
				+ modificacion.getNumRegistrosSusAjuste() + pendAprob.getNumRegistrosSusAjuste()
				+ rechazados.getNumRegistrosSusAjuste() + aprobados.getNumRegistrosSusAjuste());
		totales.setNumTotalRegistros(alta.getNumTotalRegistros() + baja.getNumTotalRegistros()
				+ modificacion.getNumTotalRegistros() + pendAprob.getNumTotalRegistros()
				+ rechazados.getNumTotalRegistros() + aprobados.getNumTotalRegistros());

		totales.setNumRegistrosCorrectosOtrDel(
				alta.getNumRegistrosCorrectosOtrDel() + baja.getNumRegistrosCorrectosOtrDel()
						+ modificacion.getNumRegistrosCorrectosOtrDel() + pendAprob.getNumRegistrosCorrectosOtrDel()
						+ rechazados.getNumRegistrosCorrectosOtrDel() + aprobados.getNumRegistrosCorrectosOtrDel());
		totales.setNumRegistrosErroneosOtrDel(
				alta.getNumRegistrosErroneosOtrDel() + baja.getNumRegistrosErroneosOtrDel()
						+ modificacion.getNumRegistrosErroneosOtrDel() + pendAprob.getNumRegistrosErroneosOtrDel()
						+ rechazados.getNumRegistrosErroneosOtrDel() + aprobados.getNumRegistrosErroneosOtrDel());
		totales.setNumRegistrosDuplicadosOtrDel(
				alta.getNumRegistrosDuplicadosOtrDel() + baja.getNumRegistrosDuplicadosOtrDel()
						+ modificacion.getNumRegistrosDuplicadosOtrDel() + pendAprob.getNumRegistrosDuplicadosOtrDel()
						+ rechazados.getNumRegistrosDuplicadosOtrDel() + aprobados.getNumRegistrosDuplicadosOtrDel());
		totales.setNumRegistrosSusAjusteOtrDel(
				alta.getNumRegistrosSusAjusteOtrDel() + baja.getNumRegistrosSusAjusteOtrDel()
						+ modificacion.getNumRegistrosSusAjusteOtrDel() + pendAprob.getNumRegistrosSusAjusteOtrDel()
						+ rechazados.getNumRegistrosSusAjusteOtrDel() + aprobados.getNumRegistrosSusAjusteOtrDel());
		totales.setNumTotalRegistrosOtrDel(alta.getNumTotalRegistrosOtrDel() + baja.getNumTotalRegistrosOtrDel()
				+ modificacion.getNumTotalRegistrosOtrDel() + pendAprob.getNumTotalRegistrosOtrDel()
				+ rechazados.getNumTotalRegistrosOtrDel() + aprobados.getNumTotalRegistrosOtrDel());

		totales.setNumTotalGenerales(alta.getNumTotalGenerales() + baja.getNumTotalGenerales()
				+ modificacion.getNumTotalGenerales() + pendAprob.getNumTotalGenerales()
				+ rechazados.getNumTotalGenerales() + aprobados.getNumTotalGenerales());

		return totales;
	}

	private DetalleMovimientoCasuisticaDTO calcularAlta(MsPmcCifrasControlInput input, Date fecProcesoIni,
			Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO alta = new DetalleMovimientoCasuisticaDTO();
		
		logger.info("-------------------------------" + "Consultas alta" + "-------------------------------");

		Long correctos = getDataAlta(input, fecProcesoIni, fecProcesoFin, 1);
		Long erroneos = getDataAlta(input, fecProcesoIni, fecProcesoFin, 2);
		Long duplicados = getDataAlta(input, fecProcesoIni, fecProcesoFin, 3);
		Long susepAjuste = getDataAlta(input, fecProcesoIni, fecProcesoFin, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getDataAlta(input, fecProcesoIni, fecProcesoFin, 5);
		Long erroneosOtrDel = getDataAlta(input, fecProcesoIni, fecProcesoFin, 6);
		Long duplicadosOtrDel = getDataAlta(input, fecProcesoIni, fecProcesoFin, 7);
		Long susepAjusteOtrDel = getDataAlta(input, fecProcesoIni, fecProcesoFin, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		alta.setTipoEntrada("Alta");
		alta.setNumRegistrosCorrectos(correctos);
		alta.setNumRegistrosErroneos(erroneos);
		alta.setNumRegistrosDuplicados(duplicados);
		alta.setNumRegistrosSusAjuste(susepAjuste);
		alta.setNumTotalRegistros(total);

		alta.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		alta.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		alta.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		alta.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		alta.setNumTotalRegistrosOtrDel(totalOtrDel);

		alta.setNumTotalGenerales(totalGeneral);

		return alta;

	}

	private Long getDataAlta(MsPmcCifrasControlInput input, Date fecProcesoIni, Date fecProcesoFin,
			Integer cveEstadoRegistro) {
		
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		Criteria cCveOrigenArchivo = null;
		Criteria cFecProcesoCarga = Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin);
		Criteria cCveEstadoRegistro = Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro);
		Criteria cCveIdAccionRegistro = Criteria.where("auditorias.cveIdAccionRegistro").is(1);
		Criteria cDelAndSubDel = null;
		Criteria cDel = null;
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador());
			} else {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo);				
			}
		}
		
		List<Criteria> criteriaList = Arrays.asList(cFecBaja, cFecProcesoCarga, cCveEstadoRegistro, cCveIdAccionRegistro, cCveOrigenArchivo);
		criteriaList = criteriaList.stream().filter(criterio -> criterio != null).collect(Collectors.toList());
		
		if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isNotBlank(input.getCveSubdelegation()) && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(input.getCveDelegation()));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));
			
			cDelAndSubDel = new Criteria().orOperator(
					new Criteria().andOperator(delAsegurado, subdelAsegurado),
					new Criteria().andOperator(delPatron, subdelPatron)
					);
			criteriaList.add(cDelAndSubDel);

		} else if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isBlank(input.getCveSubdelegation())) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss")
					.is(Integer.valueOf(input.getCveDelegation()));
			Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));
			
			cDel = new Criteria().orOperator(delAsegurado, delPatron);
			criteriaList.add(cDel);

		}
		
		Criteria matchCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		return resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
	}

	private DetalleMovimientoCasuisticaDTO calcularBaja(MsPmcCifrasControlInput input, Date fecProcesoIni,
			Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO baja = new DetalleMovimientoCasuisticaDTO();
		logger.info("-------------------------------" + "Consultas baja" + "-------------------------------");
		Long correctos = getDataBaja(input, fecProcesoIni, fecProcesoFin, 10, 1);
		Long erroneos = getDataBaja(input, fecProcesoIni, fecProcesoFin, 10, 2);
		Long duplicados = getDataBaja(input, fecProcesoIni, fecProcesoFin, 10, 3);
		Long susepAjuste = getDataBaja(input, fecProcesoIni, fecProcesoFin, 10, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getDataBaja(input, fecProcesoIni, fecProcesoFin, 11, 5);
		Long erroneosOtrDel = getDataBaja(input, fecProcesoIni, fecProcesoFin, 11, 6);
		Long duplicadosOtrDel = getDataBaja(input, fecProcesoIni, fecProcesoFin, 11, 7);
		Long susepAjusteOtrDel = getDataBaja(input, fecProcesoIni, fecProcesoFin, 11, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		baja.setTipoEntrada("Baja");
		baja.setNumRegistrosCorrectos(correctos);
		baja.setNumRegistrosErroneos(erroneos);
		baja.setNumRegistrosDuplicados(duplicados);
		baja.setNumRegistrosSusAjuste(susepAjuste);
		baja.setNumTotalRegistros(total);

		baja.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		baja.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		baja.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		baja.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		baja.setNumTotalRegistrosOtrDel(totalOtrDel);

		baja.setNumTotalGenerales(totalGeneral);

		return baja;

	}

	private Long getDataBaja(MsPmcCifrasControlInput input, Date fecProcesoIni, Date fecProcesoFin,
			Integer cveEstadoRegistro, Integer cveEstadoRegistroOriginal) {
		Long result = 0L;
		
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		Criteria cCveOrigenArchivo = null;
		Criteria cFecProcesoCarga = Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin);
		Criteria cCveEstadoRegistro = Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro);
		Criteria cCveIdAccionRegistro = Criteria.where("auditorias.cveIdAccionRegistro").is(3);
		Criteria cCveEstadoRegistroOriginal = Criteria.where("auditorias.camposOriginalesDTO.cveEstadoRegistro")
				.is(cveEstadoRegistroOriginal);
		Criteria cDelAndSubDel = null;
		Criteria cDel = null;
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador());
			} else {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo);				
			}
		}
		
		List<Criteria> criteriaList = Arrays.asList(cFecBaja, cFecProcesoCarga, cCveEstadoRegistro, cCveIdAccionRegistro,
				cCveOrigenArchivo, cCveEstadoRegistroOriginal);
		criteriaList = criteriaList.stream().filter(Objects::nonNull).collect(Collectors.toList());

		if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isNotBlank(input.getCveSubdelegation()) && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(input.getCveDelegation()));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));

			cDelAndSubDel = new Criteria().orOperator(
					new Criteria().andOperator(delAsegurado, subdelAsegurado),
					new Criteria().andOperator(delPatron, subdelPatron)
					);
			criteriaList.add(cDelAndSubDel);

		} else if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isBlank(input.getCveSubdelegation())) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss")
					.is(Integer.valueOf(input.getCveDelegation()));
			Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));

			cDel = new Criteria().orOperator(delAsegurado, delPatron);
			criteriaList.add(cDel);

		}

		Criteria matchCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		result = resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
		
		return result;
	}

	private DetalleMovimientoCasuisticaDTO calcularModificado(MsPmcCifrasControlInput input,
			Date fecProcesoIni, Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO alta = new DetalleMovimientoCasuisticaDTO();
		logger.info("-------------------------------" + "Consultas modificado" + "-------------------------------");
		Long correctos = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 1);
		Long erroneos = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 2);
		Long duplicados = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 3);
		Long susepAjuste = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 5);
		Long erroneosOtrDel = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 6);
		Long duplicadosOtrDel = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 7);
		Long susepAjusteOtrDel = getDataModificacion(input, fecProcesoIni, fecProcesoFin, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		alta.setTipoEntrada("Modificaciones");
		alta.setNumRegistrosCorrectos(correctos);
		alta.setNumRegistrosErroneos(erroneos);
		alta.setNumRegistrosDuplicados(duplicados);
		alta.setNumRegistrosSusAjuste(susepAjuste);
		alta.setNumTotalRegistros(total);

		alta.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		alta.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		alta.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		alta.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		alta.setNumTotalRegistrosOtrDel(totalOtrDel);

		alta.setNumTotalGenerales(totalGeneral);

		return alta;

	}

	private Long getDataModificacion(MsPmcCifrasControlInput input, Date fecProcesoIni,
			Date fecProcesoFin, Integer cveEstadoRegistro) {
		Long result = 0L;
		
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		Criteria cCveOrigenArchivo = null;
		Criteria cFecProcesoCarga = Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin);
		Criteria cCveEstadoRegistro = Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro);
		Criteria cCveIdAccionRegistro = Criteria.where("auditorias.cveIdAccionRegistro").is(2);
		Criteria cDelAndSubDel = null;
		Criteria cDel = null;
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador());
			} else {
				cCveOrigenArchivo = Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo);				
			}
		}
		
		List<Criteria> criteriaList = Arrays.asList(cFecBaja, cFecProcesoCarga, cCveEstadoRegistro, cCveIdAccionRegistro, cCveOrigenArchivo);
		criteriaList = criteriaList.stream().filter(criterio -> criterio != null).collect(Collectors.toList());

		if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isNotBlank(input.getCveSubdelegation()) && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(input.getCveDelegation()));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));

			cDelAndSubDel = new Criteria().orOperator(
					new Criteria().andOperator(delAsegurado, subdelAsegurado),
					new Criteria().andOperator(delPatron, subdelPatron)
					);
			criteriaList.add(cDelAndSubDel);

		} else if (StringUtils.isNotBlank(input.getCveDelegation()) && Integer.valueOf(input.getCveDelegation()) > 0 && 
				StringUtils.isBlank(input.getCveSubdelegation())) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss")
					.is(Integer.valueOf(input.getCveDelegation()));
			Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));

			cDel = new Criteria().orOperator(delAsegurado, delPatron);
			criteriaList.add(cDel);
			
		}

		Criteria matchCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		result = resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
		
		if (input.getCveTipoArchivo().equals("Alta manual") && (cveEstadoRegistro.equals(2) || cveEstadoRegistro.equals(3) || cveEstadoRegistro.equals(4) 
				|| cveEstadoRegistro.equals(6) || cveEstadoRegistro.equals(7) || cveEstadoRegistro.equals(8))) {
			result = 0L;
		}
		
		return result;
	}
	
	private DetalleMovimientoCasuisticaDTO calcularPendienteAprobar(MsPmcCifrasControlInput input,
			Date fecProcesoIni, Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO row = new DetalleMovimientoCasuisticaDTO();
		logger.info("-------------------------------" + "Consultas pendiente de aprobar" + "-------------------------------");
		Long correctos = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 1);
		Long erroneos = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 2);
		Long duplicados = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 3);
		Long susepAjuste = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 5);
		Long erroneosOtrDel = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 6);
		Long duplicadosOtrDel = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 7);
		Long susepAjusteOtrDel = getDataPendienteAprobar(input, fecProcesoIni, fecProcesoFin, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		row.setTipoEntrada("Pendientes de aprobación");
		row.setNumRegistrosCorrectos(correctos);
		row.setNumRegistrosErroneos(erroneos);
		row.setNumRegistrosDuplicados(duplicados);
		row.setNumRegistrosSusAjuste(susepAjuste);
		row.setNumTotalRegistros(total);

		row.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		row.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		row.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		row.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		row.setNumTotalRegistrosOtrDel(totalOtrDel);

		row.setNumTotalGenerales(totalGeneral);

		return row;

	}

	private Long getDataPendienteAprobar(MsPmcCifrasControlInput input, Date fecProcesoIni,
			Date fecProcesoFin, Integer cveEstadoRegistro) {
		Long result = 0L;
		List<Criteria> andCriterias0 = new ArrayList<Criteria>();
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		andCriterias0.add(cFecBaja);
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador()));
			} else {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo));				
			}
		}

		andCriterias0.add(Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin));

		if (input.getCveDelegation() != null && input.getCveDelegation() != ""
				&& Integer.valueOf(input.getCveDelegation()) > 0 && input.getCveSubdelegation() != null
				&& input.getCveSubdelegation() != "" && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(Integer.valueOf(input.getCveDelegation())));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));

			andCriterias0.add(new Criteria().orOperator(new Criteria().andOperator(subdelAsegurado, delAsegurado), new Criteria().andOperator(subdelPatron, delPatron )));

		} else if ((input.getCveDelegation() != null && input.getCveDelegation() != ""
				&& Integer.valueOf(input.getCveDelegation()) > 0)
				&& (input.getCveSubdelegation() == null || input.getCveSubdelegation() == ""
						|| Integer.valueOf(input.getCveSubdelegation()) == 0)) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss")
					.is(Integer.valueOf(input.getCveDelegation()));
			Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));
			andCriterias0.add((new Criteria().orOperator(delAsegurado, delPatron)));
		}

		andCriterias0.add(Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro));
		List<Integer> cveIdAccionRegistro = new ArrayList<>();
		cveIdAccionRegistro.add(4);
		cveIdAccionRegistro.add(5);
		cveIdAccionRegistro.add(6);
		andCriterias0.add(Criteria.where("auditorias.cveIdAccionRegistro").in(cveIdAccionRegistro)); // Pendientes
		List<Integer> cveIdAccionRegistroOk = new ArrayList<>();
		cveIdAccionRegistroOk.add(1);
		cveIdAccionRegistroOk.add(2);
		cveIdAccionRegistroOk.add(3);
		andCriterias0.add(Criteria.where("auditorias.cveIdAccionRegistro").nin(cveIdAccionRegistroOk)); // Acciones

		Criteria matchCriteria = new Criteria().andOperator(andCriterias0.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		result = resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
		
		if (input.getCveTipoArchivo().equals("Alta manual") && (cveEstadoRegistro.equals(2) || cveEstadoRegistro.equals(3) || cveEstadoRegistro.equals(4) 
				|| cveEstadoRegistro.equals(6) || cveEstadoRegistro.equals(7) || cveEstadoRegistro.equals(8))) {
			result = 0L;
		}
		
		return result;
	}

	private DetalleMovimientoCasuisticaDTO calcularRechazados(MsPmcCifrasControlInput input,
			Date fecProcesoIni, Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO row = new DetalleMovimientoCasuisticaDTO();
		logger.info("-------------------------------" + "Consultas rechazados" + "-------------------------------");
		Long correctos = getRechazados(input, fecProcesoIni, fecProcesoFin, 1);
		Long erroneos = getRechazados(input, fecProcesoIni, fecProcesoFin, 2);
		Long duplicados = getRechazados(input, fecProcesoIni, fecProcesoFin, 3);
		Long susepAjuste = getRechazados(input, fecProcesoIni, fecProcesoFin, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getRechazados(input, fecProcesoIni, fecProcesoFin, 5);
		Long erroneosOtrDel = getRechazados(input, fecProcesoIni, fecProcesoFin, 6);
		Long duplicadosOtrDel = getRechazados(input, fecProcesoIni, fecProcesoFin, 7);
		Long susepAjusteOtrDel = getRechazados(input, fecProcesoIni, fecProcesoFin, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		row.setTipoEntrada("Rechazados");
		row.setNumRegistrosCorrectos(correctos);
		row.setNumRegistrosErroneos(erroneos);
		row.setNumRegistrosDuplicados(duplicados);
		row.setNumRegistrosSusAjuste(susepAjuste);
		row.setNumTotalRegistros(total);

		row.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		row.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		row.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		row.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		row.setNumTotalRegistrosOtrDel(totalOtrDel);

		row.setNumTotalGenerales(totalGeneral);

		return row;

	}

	private Long getRechazados(MsPmcCifrasControlInput input, Date fecProcesoIni, Date fecProcesoFin,
			Integer cveEstadoRegistro) {
		Long result = 0L;
		List<Criteria> andCriterias0 = new ArrayList<Criteria>();
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		andCriterias0.add(cFecBaja);
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador()));
			} else {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo));				
			}
		}

		andCriterias0.add(Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin));

		if (input.getCveDelegation() != null && input.getCveDelegation() != ""
				&& Integer.valueOf(input.getCveDelegation()) > 0 && input.getCveSubdelegation() != null
				&& input.getCveSubdelegation() != "" && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(Integer.valueOf(input.getCveDelegation())));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));

			andCriterias0.add(new Criteria().orOperator(new Criteria().andOperator(subdelAsegurado, delAsegurado), new Criteria().andOperator(subdelPatron, delPatron )));

		} else {
			if ((input.getCveDelegation() != null && input.getCveDelegation() != ""
					&& Integer.valueOf(input.getCveDelegation()) > 0)
					&& (input.getCveSubdelegation() == null || input.getCveSubdelegation() == ""
							|| Integer.valueOf(input.getCveSubdelegation()) == 0)) {
				Criteria delAsegurado = Criteria.where("cveDelegacionNss")
						.is(Integer.valueOf(input.getCveDelegation()));
				Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));
				andCriterias0.add((new Criteria().orOperator(delAsegurado, delPatron)));
			}
		}

		andCriterias0.add(Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro));
		List<Integer> cveIdAccionRegistro = new ArrayList<>();
		cveIdAccionRegistro.add(7);
		cveIdAccionRegistro.add(8);
		cveIdAccionRegistro.add(9);
		andCriterias0.add(Criteria.where("auditorias.cveIdAccionRegistro").in(cveIdAccionRegistro)); // Pendientes
		List<Integer> cveIdAccionRegistroOk = new ArrayList<>();
		cveIdAccionRegistroOk.add(1);
		cveIdAccionRegistroOk.add(2);
		cveIdAccionRegistroOk.add(3);
		andCriterias0.add(Criteria.where("auditorias.cveIdAccionRegistro").nin(cveIdAccionRegistroOk)); // Acciones

		Criteria matchCriteria = new Criteria().andOperator(andCriterias0.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		result = resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
		
		if (input.getCveTipoArchivo().equals("Alta manual") && (cveEstadoRegistro.equals(2) || cveEstadoRegistro.equals(3) || cveEstadoRegistro.equals(4) 
				|| cveEstadoRegistro.equals(6) || cveEstadoRegistro.equals(7) || cveEstadoRegistro.equals(8))) {
			result = 0L;
		}
		
		return result;
	}

	private DetalleMovimientoCasuisticaDTO calcularAprobados(MsPmcCifrasControlInput input, Date fecProcesoIni,
			Date fecProcesoFin) {
		DetalleMovimientoCasuisticaDTO row = new DetalleMovimientoCasuisticaDTO();
		logger.info("-------------------------------" + "Consultas aprobados" + "-------------------------------");
		Long correctos = getAprobados(input, fecProcesoIni, fecProcesoFin, 1);
		Long erroneos = getAprobados(input, fecProcesoIni, fecProcesoFin, 2);
		Long duplicados = getAprobados(input, fecProcesoIni, fecProcesoFin, 3);
		Long susepAjuste = getAprobados(input, fecProcesoIni, fecProcesoFin, 4);
		Long total = correctos + erroneos + duplicados + susepAjuste;

		Long correctosOtrDel = getAprobados(input, fecProcesoIni, fecProcesoFin, 5);
		Long erroneosOtrDel = getAprobados(input, fecProcesoIni, fecProcesoFin, 6);
		Long duplicadosOtrDel = getAprobados(input, fecProcesoIni, fecProcesoFin, 7);
		Long susepAjusteOtrDel = getAprobados(input, fecProcesoIni, fecProcesoFin, 8);
		Long totalOtrDel = correctosOtrDel + erroneosOtrDel + duplicadosOtrDel + susepAjusteOtrDel;

		Long totalGeneral = total + totalOtrDel;

		row.setTipoEntrada("Aprobados");
		row.setNumRegistrosCorrectos(correctos);
		row.setNumRegistrosErroneos(erroneos);
		row.setNumRegistrosDuplicados(duplicados);
		row.setNumRegistrosSusAjuste(susepAjuste);
		row.setNumTotalRegistros(total);

		row.setNumRegistrosCorrectosOtrDel(correctosOtrDel);
		row.setNumRegistrosErroneosOtrDel(erroneosOtrDel);
		row.setNumRegistrosDuplicadosOtrDel(duplicadosOtrDel);
		row.setNumRegistrosSusAjusteOtrDel(susepAjusteOtrDel);
		row.setNumTotalRegistrosOtrDel(totalOtrDel);

		row.setNumTotalGenerales(totalGeneral);

		return row;

	}

	private Long getAprobados(MsPmcCifrasControlInput input, Date fecProcesoIni, Date fecProcesoFin,
			Integer cveEstadoRegistro) {
		Long result = 0L;
		List<Criteria> andCriterias0 = new ArrayList<Criteria>();
		Criteria cFecBaja = Criteria.where("auditorias.fecBaja").is(null);
		andCriterias0.add(cFecBaja);
		
		String cveOrigenArchivo = input.getCveTipoArchivo();
		if (StringUtils.isNotBlank(cveOrigenArchivo) && StringUtils.isNotEmpty(cveOrigenArchivo) && !cveOrigenArchivo.equals("-1")) {
			if (cveOrigenArchivo.equals("Alta manual")) {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(IdentificadorArchivoEnum.MANUAL.getIdentificador()));
			} else {
				andCriterias0.add(Criteria.where("cveOrigenArchivo").is(cveOrigenArchivo));				
			}
		}

		andCriterias0.add(Criteria.where("fecProcesoCarga").gt(fecProcesoIni).lte(fecProcesoFin));

		if (input.getCveDelegation() != null && input.getCveDelegation() != ""
				&& Integer.valueOf(input.getCveDelegation()) > 0 && input.getCveSubdelegation() != null
				&& input.getCveSubdelegation() != "" && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("cveDelegacionNss").is(Integer.valueOf(input.getCveDelegation()));

			Criteria delPatron = Criteria.where("cveDelRegPatronal")
					.is(Integer.valueOf(Integer.valueOf(input.getCveDelegation())));

			Criteria subdelAsegurado = Criteria.where("cveSubdelNss")
					.is(Integer.valueOf(input.getCveSubdelegation()));

			Criteria subdelPatron = Criteria.where("cveSubDelRegPatronal").is(Integer.valueOf(input.getCveSubdelegation()));

			andCriterias0.add(new Criteria().orOperator(new Criteria().andOperator(subdelAsegurado, delAsegurado), new Criteria().andOperator(subdelPatron, delPatron )));

		} else {
			if ((input.getCveDelegation() != null && input.getCveDelegation() != ""
					&& Integer.valueOf(input.getCveDelegation()) > 0)
					&& (input.getCveSubdelegation() == null || input.getCveSubdelegation() == ""
							|| Integer.valueOf(input.getCveSubdelegation()) == 0)) {
				Criteria delAsegurado = Criteria.where("cveDelegacionNss")
						.is(Integer.valueOf(input.getCveDelegation()));
				Criteria delPatron = Criteria.where("cveDelRegPatronal").is(Integer.valueOf(input.getCveDelegation()));
				andCriterias0.add((new Criteria().orOperator(delAsegurado, delPatron)));
			}
		}

		andCriterias0.add(Criteria.where("cveEstadoRegistro").is(cveEstadoRegistro));
		List<Integer> cveIdAccionRegistroOk = new ArrayList<>();
		cveIdAccionRegistroOk.add(1);
		cveIdAccionRegistroOk.add(2);
		cveIdAccionRegistroOk.add(3);
		andCriterias0.add(Criteria.where("auditorias.cveIdAccionRegistro").in(cveIdAccionRegistroOk)); // Acciones

		Criteria matchCriteria = new Criteria().andOperator(andCriterias0.toArray(new Criteria[0]));
		UnwindOperation unwind = Aggregation.unwind("auditorias");
		MatchOperation match = Aggregation.match(matchCriteria);
		CountOperation count = Aggregation.count().as("totalElements");
		
		TypedAggregation<CountDTO> aggregation = Aggregation.newAggregation(CountDTO.class, unwind, match, count);

		logger.info(aggregation.toString());

		AggregationResults<CountDTO> resultCount = mongoOperations.aggregate(aggregation, CambioDTO.class, CountDTO.class);
		
		result = resultCount.getUniqueMappedResult() != null ? resultCount.getUniqueMappedResult().getTotalElements() : 0L;
		
		if (input.getCveTipoArchivo().equals("Alta manual") && (cveEstadoRegistro.equals(2) || cveEstadoRegistro.equals(3) || cveEstadoRegistro.equals(4) 
				|| cveEstadoRegistro.equals(6) || cveEstadoRegistro.equals(7) || cveEstadoRegistro.equals(8))) {
			result = 0L;
		}
		
		return result;
	}

}
