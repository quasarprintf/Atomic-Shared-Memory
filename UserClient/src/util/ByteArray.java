package util;

public class ByteArray {
	
	public static String parseToString(byte[] byteArray)
	{
		String result = "";
		int i = 0;
		while (i < byteArray.length && byteArray[i] != 0)
		{
			result += (char)byteArray[i];
			i++;
		}
		return result;
	}

}
