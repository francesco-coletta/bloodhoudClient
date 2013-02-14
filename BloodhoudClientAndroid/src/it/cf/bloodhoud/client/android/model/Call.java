package it.cf.bloodhoud.client.android.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Call {
	static private final Logger LOG = LoggerFactory.getLogger(Call.class);

	@Element
	private final String phoneNumber;
	@Element
	private String nameContact = "";
	@Attribute
	private final long timestampStartCall;
	@Attribute
	private final String timestampStartCallFormatted;
	@Attribute
	private long timestampEndCall;
	@Attribute
	private final String timestampEndCallFormatted;
	@Element
	private final CallDirection direction;
	@Element
	private CallState state;

	public enum CallDirection {
		INCOMING, OUTGOING;// , UNKNOW
	}

	public enum CallState {
		RINGING, REFUSED, ACCEPTED, FINISHED, UNKNOW;
	}

	public Call(String phoneNumber, String nameContact,
			long timestampStartCall, long timestampEndCall,
			CallDirection direction, CallState state) {
		super();
		this.phoneNumber = phoneNumber;
		this.nameContact = nameContact;
		this.timestampStartCall = timestampStartCall;
		this.timestampStartCallFormatted = Utils.formatDatetime(timestampStartCall);
		this.timestampEndCall = timestampEndCall;
		this.timestampEndCallFormatted = Utils.formatDatetime(timestampEndCall);
		this.direction = direction;
		this.state = state;
	}

	public Call(CallDirection direction, String phoneNumber) {
		super();
		this.phoneNumber = StringUtils.trimToEmpty(phoneNumber);
		this.timestampStartCall = System.currentTimeMillis();
		this.timestampStartCallFormatted = Utils.formatDatetime(timestampStartCall);
		this.timestampEndCall = this.timestampStartCall;
		this.timestampEndCallFormatted = Utils.formatDatetime(timestampEndCall);
		this.direction = direction;

		if (this.direction == CallDirection.INCOMING) {
			state = CallState.RINGING;
		} else {
			state = CallState.RINGING;
		}
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public long getTimestampStartCall() {
		return timestampStartCall;
	}

	public String getTimestampStartCallFormatted() {
		return timestampStartCallFormatted;
	}

	public long getTimestampEndCall() {
		return timestampEndCall;
	}

	public String getTimestampEndCallFormatted() {
		return timestampEndCallFormatted;
	}
	
	
	public long getCallDuration() {
		return (timestampEndCall - timestampStartCall);
	}

	public long getCallDurationSec() {
		return ((timestampEndCall - timestampStartCall) / 1000);
	}

	public CallDirection getDirection() {
		return direction;
	}

	public String getNameContact() {
		return nameContact;
	}

	public void setNameContact(String nameContact) {
		if (StringUtils.isBlank(nameContact) == false) {
			this.nameContact = nameContact;
		}
	}

	@Override
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
		toString.append("Direction", direction.name());
		toString.append(" State", state.toString());
		toString.append(" Start Time", Utils.formatDatetime(timestampStartCall));
		toString.append(" End Time", Utils.formatDatetime(timestampEndCall));
		toString.append(" Durata (sec)", getCallDurationSec());
		toString.append(" Phone", StringUtils.trimToEmpty(phoneNumber));
		toString.append(" Contact", StringUtils.trimToEmpty(nameContact));
		return toString.build().concat("\n");
	}

	public CallState getState() {
		return state;
	}

	protected void setState(CallState state) {
		this.state = state;
	}

	protected void setTimestampEndCall(long timestampEndCall) {
		if (timestampEndCall >= timestampStartCall) {
			this.timestampEndCall = timestampEndCall;
		} else {
			LOG.error("Si � tentato di impostare l'ora di fine chiamata prima dell'ora di inizio");
		}
	}

}