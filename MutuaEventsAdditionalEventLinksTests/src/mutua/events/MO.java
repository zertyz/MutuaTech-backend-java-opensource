package mutua.events;

/** <pre>
 * MO.java
 * =======
 * (created by luiz, Sep 9, 2015)
 *
 * Represents an MO that can be stored and retrieved from the queues
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class MO {
	public final String phone;
	public final String text;
	public MO(String phone, String text) {
		this.phone = phone;
		this.text  = text;
	}
	@Override
	public String toString() {
		return "phone='"+phone+"', text='"+text+"'";
	}
}

