package gwt.seca.client.util;

public class RandomString {
	private RandomString() {}
	private static final char[] alphaNumChars = ("abcdefghijklmnopqrstuvwxyz" + 
	                                          "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
	                                          "0123456789").toCharArray();
	
	public static String getAlphaNum(int length)
	{
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < length; i++) {
			s.append(alphaNumChars[ (int)Math.floor(Math.random()*63) ]);
		}
		return s.toString();
	}
}
