package mutua.events;

import mutua.events.TestAdditionalEventServer.ETestEventServices;
import mutua.imi.IndirectMethodInvocationInfo;
import adapters.IJDBCAdapterParameterDefinition;
import adapters.exceptions.PreparedProcedureException;

/** <pre>
 * SpecializedMOQueueDataBureau.java
 * =================================
 * (created by luiz, Aug 20, 2015)
 *
 * Defines a queue similar to the one used to store MOs, to be used on performance tests
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
*/

public class SpecializedMOQueueDataBureau extends IDatabaseQueueDataBureau<ETestEventServices> {

	enum SpecializedMOParameters implements IJDBCAdapterParameterDefinition {

		PHONE,
		TEXT;

		@Override
		public String getParameterName() {
			return name();
		}
	}
	
	@Override
	public Object[] serializeQueueEntry(IndirectMethodInvocationInfo<ETestEventServices> entry) throws PreparedProcedureException {
		MO mo = (MO)entry.getParameters()[0];
		return new Object[] {
			SpecializedMOParameters.PHONE, mo.phone,
			SpecializedMOParameters.TEXT,  mo.text};
	}
	
	@Override
	public IndirectMethodInvocationInfo<ETestEventServices> deserializeQueueEntry(int eventId, Object[] databaseRow) {
		String phone   = (String)databaseRow[0];
		String text    = (String)databaseRow[1];
		MO mo = new MO(phone, text);
		IndirectMethodInvocationInfo<ETestEventServices> entry = new IndirectMethodInvocationInfo<ETestEventServices>(ETestEventServices.MO_ARRIVED, mo);
		return entry;
	}
	
	@Override
	public IJDBCAdapterParameterDefinition[] getParametersListForInsertNewQueueElementQuery() {
		return SpecializedMOParameters.values();
	}
	
	@Override
	public String getQueueElementFieldList() {
		return "phone, text";
	}
	
	@Override
	public String getFieldsCreationLine() {
		return "phone  TEXT NOT NULL, " +
		       "text   TEXT NOT NULL, ";
	}
}