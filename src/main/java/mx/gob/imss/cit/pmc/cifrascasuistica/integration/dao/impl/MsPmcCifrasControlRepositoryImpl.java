package mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import mx.gob.imss.cit.mspmccommons.enums.CamposAseguradoEnum;
import mx.gob.imss.cit.mspmccommons.enums.CamposPatronEnum;
import mx.gob.imss.cit.mspmccommons.integration.model.*;
import mx.gob.imss.cit.mspmccommons.utils.AggregationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.mspmccommons.enums.EstadoRegistroEnum;


import mx.gob.imss.cit.mspmccommons.utils.CustomAggregationOperation;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.MsPmcCifrasControlRepository;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.model.EstadoRegistroDTO;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.model.FacetResponse;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.model.TipoArchivoDTO;
import mx.gob.imss.cit.pmc.cifrascasuistica.security.model.CifrasControlDTO;

@Repository
public class MsPmcCifrasControlRepositoryImpl implements MsPmcCifrasControlRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private ParametroRepository parametroRepository;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public List<CifrasControlMovimientosResponseDTO> getCifrasControl(MsPmcCifrasControlInput input) throws BusinessException {
		List<DetalleSalidaOutput> detalleOutput = new ArrayList<DetalleSalidaOutput>();

		// Se calculan las fechas inicio y fin para la consulta
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		Criteria cFecProcesoCarga = null;

		if (fecProcesoIni != null && fecProcesoFin != null) {
			cFecProcesoCarga = new Criteria().andOperator(Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
							.gt(fecProcesoIni), Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
							.lte(fecProcesoFin));
		}
		logger.info("cveDelegacion recibida: " + input.getCveDelegation());

		Criteria cDelAndSubDel = null;
		Criteria cDel = null;
		
		//**********************  VALIDAMOS SI TRAE DELEGECION Y SUBDELEGACION   ******************************************************	
		
        if(!(input.getCveDelegation() == null || input.getCveDelegation() == "" || Integer.valueOf(input.getCveDelegation()) == 0) &&
        	 (input.getCveSubdelegation() == null || input.getCveSubdelegation() == "" || Integer.valueOf(input.getCveSubdelegation()) == 0)) {
        	logger.info("--------------TRAEMOS SOLO DELEGACION    ---------------------------");
        	Criteria delAsegurado = Criteria.where(CamposAseguradoEnum.DELEGACION_NSS.getNombreCampo()).is(Integer.valueOf(input.getCveDelegation()));
			Criteria delPatron = Criteria.where(CamposPatronEnum.DELEGACION.getNombreCampo()).is(Integer.valueOf(input.getCveDelegation()));
			cDel = new Criteria().orOperator(delAsegurado, delPatron);
		}
		  
		if( !(input.getCveDelegation() == null || input.getCveDelegation() == "" || Integer.valueOf(input.getCveDelegation()) == 0) &&
			!(input.getCveSubdelegation() == null || input.getCveSubdelegation() == "" || Integer.valueOf(input.getCveSubdelegation()) == 0)) {
			logger.info("--------------TRAEMOS DELEGACION Y SUBDELAGACION -------------------");
			Criteria delAsegurado = Criteria.where(CamposAseguradoEnum.DELEGACION_NSS.getNombreCampo()).is(Integer.valueOf(input.getCveDelegation()));
			Criteria subdelAsegurado = Criteria.where(CamposAseguradoEnum.SUBDELEGACION_NSS.getNombreCampo()).is(Integer.valueOf(input.getCveSubdelegation()));
			
			Criteria delPatron = Criteria.where(CamposPatronEnum.DELEGACION.getNombreCampo()).is(Integer.valueOf(input.getCveDelegation()));
			Criteria subdelPatron = Criteria.where(CamposPatronEnum.SUBDELEGACION.getNombreCampo()).is(Integer.valueOf(input.getCveSubdelegation()));

			cDelAndSubDel = new Criteria().orOperator(
					new Criteria().andOperator(delAsegurado, subdelAsegurado),
					new Criteria().andOperator(delPatron, subdelPatron));
		}
		
		TypedAggregation<CifrasControlMovimientosResponseDTO> aggregationCont = buildAggregationCount(cFecProcesoCarga,
				cDelAndSubDel, cDel);
		logger.info(aggregationCont.toString());
		AggregationResults<CifrasControlMovimientosResponseDTO> listArchivosAggregation = mongoOperations.aggregate(
				aggregationCont, DetalleRegistroDTO.class, CifrasControlMovimientosResponseDTO.class);
		return listArchivosAggregation.getMappedResults();
	}
	
	private TypedAggregation<CifrasControlMovimientosResponseDTO> buildAggregationCount(Criteria cFecProcesoCarga, Criteria cDelAndSubDel,
			Criteria cDel) {

		String groupJson = buildGroupString();
		CustomAggregationOperation group = new CustomAggregationOperation(groupJson);
		SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "objectIdArchivoDetalle");
		// Las operaciones deben ir en orden en la lista
		List<AggregationOperation> aggregationOperationList = Arrays.asList(
				AggregationUtils.validateMatchOp(cFecProcesoCarga),
				AggregationUtils.validateMatchOp(cDelAndSubDel),
				AggregationUtils.validateMatchOp(cDel),
				group,
				sort
				);
		aggregationOperationList = aggregationOperationList.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		return Aggregation.newAggregation(CifrasControlMovimientosResponseDTO.class, aggregationOperationList);
	}

	private String buildGroupString() {
		String group = "{ $group: { _id: '$cveOrigenArchivo',";
		for(EstadoRegistroEnum estadoRegistro : EstadoRegistroEnum.values()) {
			if (estadoRegistro.getCveEstadoRegistro() >= 1 && estadoRegistro.getCveEstadoRegistro() != 9) {
				group = group.concat(formatName(estadoRegistro.name()))
						.concat(": { $sum: { $cond: [{$eq: ['$aseguradoDTO.cveEstadoRegistro', ")
						.concat(String.valueOf(estadoRegistro.getCveEstadoRegistro()))
						.concat("]}, { $sum: 1 }, { $sum: 0 }] } },");
			}
		}
		group = group.concat("total: { $sum: 1 } } }");
		return group;
	}

	private String formatName(String name) {
		StringBuilder stringBuilder = new StringBuilder(name.toLowerCase());
		for (int i = 0; i < stringBuilder.length(); i++) {
			if (stringBuilder.charAt(i) == '_') {
				stringBuilder.deleteCharAt(i);
				stringBuilder.replace(i, i + 1, String.valueOf(Character.toUpperCase(stringBuilder.charAt(i))));
			}
		}
		return stringBuilder.toString();
	}

}
