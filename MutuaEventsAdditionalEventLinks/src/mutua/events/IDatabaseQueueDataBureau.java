package mutua.events;

import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.imi.IndirectMethodInvocationInfo;
import adapters.IJDBCAdapterParameterDefinition;
import adapters.exceptions.PreparedProcedureException;

/** <pre>
 * IDatabaseQueueDataBureau.java
 * =============================
 * (created by luiz, Jan 30, 2015)
 *
 * Knows how to serialize & deserialize queue events to and from database queries
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public abstract class IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> {

	/** Returns the array of parameter values from 'entry' that will be used to insert a queue
	 * element on the database via {@link QueuesPostgreSQLAdapter#InsertNewQueueElement}.
	 *  The Object[] must return the pairs parameter/value for the parameters enumerated by {@link #getParametersListForInsertNewQueueElementQuery()} */
	public abstract Object[] serializeQueueEntry(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> entry) throws PreparedProcedureException;
	
	/** Receives 'databaseRow' (the return of the query 'FetchNextQueueElements') and returns the original queue 'entry' with the 'eventId' */
	public abstract IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> deserializeQueueEntry(int eventId, Object[] databaseRow);
	
	/** returns the expression between VALUES() of 'InsertNewQueueElement' query, which will have their parameters fulfilled by 'serializeQueueEntry' */
	public abstract IJDBCAdapterParameterDefinition[] getParametersListForInsertNewQueueElementQuery();
	
	/** returns the field list expression for INSERT and SELECT clauses when dealing with queue elements -- which will be used to fill 'databaseRow' used by 'desserializeQueueEntry' */
	public abstract String getQueueElementFieldList();

	/** returns the fields creation line for the queue table -- all fields should end with a comma */
	public abstract String getFieldsCreationLine();
	
}
