package mutua.events;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.imi.IndirectMethodInvocationInfo;
import adapters.dto.PreparedProcedureInvocationDto;
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

class SpecializedMOQueueDataBureau extends IDatabaseQueueDataBureau<ETestEventServices> {
	@Override
	public void serializeQueueEntry(IndirectMethodInvocationInfo<ETestEventServices> entry, PreparedProcedureInvocationDto preparedProcedure) throws PreparedProcedureException {
		MO mo = (MO)entry.getParameters()[0];
		preparedProcedure.addParameter("CARRIER", "testCarrier");
		preparedProcedure.addParameter("PHONE",   mo.phone);
		preparedProcedure.addParameter("TEXT",    mo.text);
	}
	@Override
	public IndirectMethodInvocationInfo<ETestEventServices> deserializeQueueEntry(int eventId, Object[] databaseRow) {
		String carrier = (String)databaseRow[1];
		String phone   = (String)databaseRow[2];
		String text    = (String)databaseRow[3];
		MO mo = new MO(phone, text);
		IndirectMethodInvocationInfo<ETestEventServices> entry = new IndirectMethodInvocationInfo<ETestEventServices>(ETestEventServices.MO_ARRIVED, mo);
		return entry;
	}
	@Override
	public String getValuesExpressionForInsertNewQueueElementQuery() {
		return "${METHOD_ID}, ${CARRIER}, ${PHONE}, ${TEXT}";
	}
	@Override
	public String getQueueElementFieldList() {
		return "methodId, carrier, phone, text";
	}
	@Override
	public String getFieldsCreationLine() {
		return 	"carrier   TEXT NOT NULL, " +
                "phone     TEXT NOT NULL, " +
				"text      TEXT NOT NULL, ";
	}
}