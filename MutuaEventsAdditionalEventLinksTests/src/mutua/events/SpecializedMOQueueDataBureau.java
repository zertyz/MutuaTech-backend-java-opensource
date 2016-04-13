package mutua.events;

import mutua.events.TestAdditionalEventServer.ETestAdditionalEventServices;
import mutua.imi.IndirectMethodInvocationInfo;
import adapters.IJDBCAdapterParameterDefinition;

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

public class SpecializedMOQueueDataBureau extends IDatabaseQueueDataBureau<ETestAdditionalEventServices> {

	enum SpecializedMOParameters implements IJDBCAdapterParameterDefinition {

		PHONE,
		TEXT;

		@Override
		public String getParameterName() {
			return name();
		}
	}
	
	@Override
	public Object[] serializeQueueEntry(IndirectMethodInvocationInfo<ETestAdditionalEventServices> entry) {
		MO mo = (MO)entry.getParameters()[0];
		return new Object[] {
			SpecializedMOParameters.PHONE, mo.phone,
			SpecializedMOParameters.TEXT,  mo.text};
	}
	
	@Override
	public IndirectMethodInvocationInfo<ETestAdditionalEventServices> deserializeQueueEntry(int eventId, Object[] databaseRow) {
		String phone   = (String)databaseRow[0];
		String text    = (String)databaseRow[1];
		MO mo = new MO(phone, text);
		IndirectMethodInvocationInfo<ETestAdditionalEventServices> entry = new IndirectMethodInvocationInfo<ETestAdditionalEventServices>(ETestAdditionalEventServices.MO_ARRIVED, mo);
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