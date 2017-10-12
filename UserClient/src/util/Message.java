package util;

public class Message {
	private int reqID = -1;
	private String flag = null;
	private int pcID = -1;
	private String key = null;
	private String value = null;
	private int seqID = -2;
	
	Message(String messageString)
	{
		String[] messageArray = messageString.split(":");
		if (messageArray.length < 3)
			{throw new RuntimeException("ERROR - message smaller than expected");}
		else if (messageArray[2].equals("read-request"))
			{buildReadRequest(messageArray);}
		else if (messageArray[2].equals("read-return"))
			{buildReadReturn(messageArray);}
		else if (messageArray[2].equals("write-request"))
			{buildWriteRequest(messageArray);}
		else
			{throw new RuntimeException("ERROR - message type unknown");}
		
	}
	
	
	//message initializers
	
	private void buildReadRequest(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		key = messageArray[3];
	}
	
	private void buildReadReturn(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		value = messageArray[3];
		seqID = Integer.parseInt(messageArray[4]);
	}
	
	private void buildWriteRequest(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		key = messageArray[3];
		value = messageArray[4];
		seqID = Integer.parseInt(messageArray[5]);
	}
	
	
	//setters
	
	public void setReqID(int newID)
	{
		reqID = newID;
	}
	
	public void setFlag(String newFlag)
	{
		flag = newFlag;
	}
	
	public void setPcID(int newID)
	{
		pcID = newID;
	}
	
	public void setKey(String newKey)
	{
		key = newKey;
	}
	
	public void setValue(String newValue)
	{
		value = newValue;
	}
	
	public void setSeqID(int newID)
	{
		seqID = newID;
	}
	
	
	//getters
	
	public int getReqID()
	{
		if (reqID == -1)
			{throw new RuntimeException("ERROR - trying to access uninitialized reqID");}
		return reqID;
	}
	
	public String getFlag()
	{
		if (flag == null)
		{throw new RuntimeException("ERROR - trying to access uninitialized flag");}
		return flag;
	}
	
	public int getPcID()
	{
		if (pcID == -1)
			{throw new RuntimeException("ERROR - trying to access uninitialized pcID");}
		return pcID;
	}
	
	public String getKey()
	{
		if (key == null)
			{throw new RuntimeException("ERROR - trying to access uninitialized key");}
		return key;
	}
	
	public String getValue()
	{
		if (value == null)
			{throw new RuntimeException("ERROR - trying to access uninitialized value");}
		return value;
	}
	
	public int getSeqID()
	{
		if (seqID == -2)
			{throw new RuntimeException("ERROR - trying to access uninitialized seqID");}
		return seqID;
	}
	
	
	public String formatMessage() //returns a string with the message contents properly formatted
	{
		if (flag == "read-request")
			{return formatReadRequest();}
		else if (flag == "read-return")
			{return formatReadReturn();}
		else if (flag == "write-request")
			{return formatWriteRequest();}
		else
			{throw new RuntimeException("ERROR - message type unknown");}
	}
	
	//formatMessage helpers
	
	private String formatReadRequest()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":" + key;
	}
	
	private String formatReadReturn()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":" + String.valueOf(seqID) + ":" + value;
	}
	
	private String formatWriteRequest()
	{
		return reqID + ":" + flag + ":" + String.valueOf(pcID) + ":" + String.valueOf(seqID) + ":" + key + ":" + value;
	}
	
}
