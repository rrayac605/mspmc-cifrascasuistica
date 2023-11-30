package mx.gob.imss.cit.pmc.cifrascasuisticay.util;


import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class CustomAggregationOperation implements AggregationOperation {
	
	private String jsonOpperation;
	
	public CustomAggregationOperation(String jsonOpperaton) {
		this.jsonOpperation = jsonOpperaton;
	}

	@Override
	public Document toDocument(AggregationOperationContext context) {
		return context.getMappedObject(Document.parse(jsonOpperation));
	}	

}
