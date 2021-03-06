package it.cf.bloodhoud.client.android.dao;

public interface TableCall {
	
	public String TABLE_NAME = "call";
	
	public String COLUMN_ID = "id";
	public String COLUMN_DIRECTION = "direction"; //outgoing/incoming
	public String COLUMN_TIMESTAMP_START = "timestampStart";
	public String COLUMN_TIMESTAMP_END = "timestampEnd";
	public String COLUMN_PHONENUMBER = "phoneNum";
	public String COLUMN_CONTACT = "contact";
	public String COLUMN_STATE = "state";
	public String COLUMN_DURATION = "duration";
	public String COLUMN_SERVER_SYNCRO = "serverSyncro"; // 0 = sms non inviato al server, 1 = sms inviato al server
	public String COLUMN_SERVER_SYNCRO_TIMESTAMP = "serverSyncroTimestamp";
	public String COLUMN_SERVER_ID = "serverId";
	}
