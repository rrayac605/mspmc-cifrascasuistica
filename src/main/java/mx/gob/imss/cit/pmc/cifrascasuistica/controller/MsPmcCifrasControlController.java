package mx.gob.imss.cit.pmc.cifrascasuistica.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlMovimientosResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mx.gob.imss.cit.mspmccommons.dto.ErrorResponse;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;

import mx.gob.imss.cit.mspmccommons.integration.model.MovimientoCasuisticaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.MsPmcCifrasControlInput;
import mx.gob.imss.cit.pmc.cifrascasuistica.integration.dao.impl.DetalleSalidaOutput;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.MsPmcCifrasControlService;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.ReporteMovimientosCICService;
import mx.gob.imss.cit.pmc.cifrascasuistica.services.ReporteService;
import net.sf.jasperreports.engine.JRException;

@RestController
@RequestMapping("/mscifrascasuistica/v1")
public class MsPmcCifrasControlController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MsPmcCifrasControlService msPmcCifrasControlService;
	
	@Autowired
	ReporteService reportService;
	
	@Autowired
	ReporteMovimientosCICService reporteMovimientosCICService;
	
    @RequestMapping("/health/ready")
    @ResponseStatus(HttpStatus.OK)
    public void ready() {}

    @RequestMapping("/health/live")
    @ResponseStatus(HttpStatus.OK)
    public void live() {}
    
    @CrossOrigin(origins = "*", allowedHeaders="*")
    @PostMapping("/cifrascasuistica")
    public Object getCifrasOriginales(@RequestBody MsPmcCifrasControlInput input,@RequestHeader(value = "Authorization") String token) {
    	
    	Object respuesta = null;
    	
        logger.debug("mspmccapados service ready to return");
        
        List<CifrasControlMovimientosResponseDTO> model;
        
        try {
	        model = msPmcCifrasControlService.getCifrasControl(input);
	
	        respuesta = new ResponseEntity<>(model, HttpStatus.OK);
	        
        }
        catch (BusinessException be) {
        	
        	ErrorResponse errorResponse = be.getErrorResponse();
        	
        	int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());
  
            respuesta = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));
 
        }
        
        return respuesta;
    }
	
    @CrossOrigin(origins = "*", allowedHeaders="*")
    @PostMapping("/reportpdf")
    public Object getCifrasOriginalesReport(@RequestBody MsPmcCifrasControlInput input,@RequestHeader(value = "Authorization") String token) throws BusinessException {
    	
    	Object respuesta = null;
    	
        logger.debug("mspmccapados service ready to return");
        
        Object model;
        
        try {
	        model = reportService.getCifrasControlReport(input);
	
	        respuesta = new ResponseEntity<Object>(model, HttpStatus.OK);
	        
        }
        catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return respuesta;
    }
    
//CU 13
    
    @ApiOperation(value = "Consulta movimientos de las cifras de integracion", nickname = "movimientosCasuistica", notes = "Consulta movimientos de las cifras de integracion", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = Object.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = "/movimientosCasuistica", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object consultarMovimientosCIC(@RequestBody MsPmcCifrasControlInput input,@RequestHeader(value = "Authorization") String token) {
    	Object respuesta = null;
        logger.debug("consultarMovimientosCIC service ready to return");
        
        MovimientoCasuisticaOutput model;
        
        try {
	        model = msPmcCifrasControlService.getMovimientoCasuistica(input);
	        respuesta = new ResponseEntity<MovimientoCasuisticaOutput>(model, HttpStatus.OK);
        }
        catch (BusinessException be) {
        	ErrorResponse errorResponse = be.getErrorResponse();
        	int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());
            respuesta = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));
        }
        
        return respuesta;
    }
	
    @ApiOperation(value = "Reporte movimientos de las cifras de integracion", nickname = "validarLocal", notes = "Guardado de registro con cambio", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = Object.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = "/movimientosCICReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object consultarMovimientosCICReport(@RequestBody MsPmcCifrasControlInput input,@RequestHeader(value = "Authorization") String token) throws BusinessException {
    	
    	Object respuesta = null;
        logger.debug("movimientosCICReport service ready to return");
        Object model;
        
        try {
	        model = reporteMovimientosCICService.getMovimientosCICReport(input);
	        respuesta = new ResponseEntity<Object>(model, HttpStatus.OK);
        }
        catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return respuesta;
    }
    
    
    
    
}
