package mutua.events;

import mutua.imi.IndirectMethodInvocationInfo;
import adapters.dto.PreparedProcedureInvocationDto;
import adapters.exceptions.PreparedProcedureException;

/** <pre>
 * IDatabaseQueueDataBureau.java
 * =============================
 * (created by luiz, Jan 30, 2015)
 *
 * Knows how to serialize & desserialize queue events to and from database queries
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public abstract class IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> {

	/** Adds the parameters from 'entry' to the 'preparedProcedure' that will invoke 'InsertNewQueueElement' to insert it on the database */
	public abstract void serializeQueueEntry(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> entry, PreparedProcedureInvocationDto preparedProcedure) throws PreparedProcedureException;
	
	/** Receives 'databaseRow' (the return of the query 'FetchNextQueueElements') and returns the original queue 'entry' with the 'eventId' */
	public abstract IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> deserializeQueueEntry(int eventId, Object[] databaseRow);
	
	/** returns the expression between VALUES() of 'InsertNewQueueElement' query, which will have their parameters fulfilled by 'serializeQueueEntry' */
	public String getValuesExpressionForInsertNewQueueElementQuery()  {
		return "${METHOD_ID}, ${PARAMETERS}";
	}
	
	/** returns the field list expression for INSERT and SELECT clauses when dealing with queue elements -- which will be used to fill 'databaseRow' used by 'desserializeQueueEntry' */
	public String getQueueElementFieldList() {
		return "methodId, parameters";
	}

	/** returns the fields creation line for the queue table -- all fields should end with a comma */
	public String getFieldsCreationLine() {
		return 	//"methodId   VARCHAR(15)   NOT NULL, " +	(methodId is included by default)
                "parameters VARCHAR(1024) NOT NULL, ";
	}
	
}
