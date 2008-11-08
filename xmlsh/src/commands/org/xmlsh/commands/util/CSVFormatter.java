package org.xmlsh.commands.util;

/**
 * 
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class CSVFormatter
{

	/**
	 *  Forcibly quote/encode a character string
	 */
	
    private static String encodeQuote(String str)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('"');

		char ch;
		int len = str.length();
		for (int i = 0; i < len; i++)
		{
			ch = str.charAt(i);
			if (ch == '"'){		// double-quote quote chars
				sb.append('"');
				sb.append('"');
			}
            else
            if( ch == '\n')	// newlines to nl 
                sb.append("\\n");
            else
            if( ch == '\r')	// cr to \\r 
                 sb.append("\\r");
            
            else                  
				sb.append(ch);
		}
		sb.append('"');
		return sb.toString();
	}

	/**
     *  CSV Encode a single string
     * If no ","  " " or \" then leave alone
     * Otherwise double-quote and double-double qouote literal quotes
     */

	public static String encodeField(String str)
	{
		if (str == null)
			return ""; //$NON-NLS-1$

		// Check to see if we have any \" or \, 
		char ch;
		int len = str.length();
		for (int i = 0; i < len; i++)
		{
			ch = str.charAt(i);
			if (ch == '"' || ch == ',' || ch == '\n' || ch == '\r')
				return CSVFormatter.encodeQuote(str);
                            
		}

		return str;
	}

	/**
     * Helper method to write out an array of  strings as a CSV "record" onto the writer
     */
    
	public static String encodeRow(String[] csv)
	{

		int n = csv.length;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; i++)
		{
			sb.append(CSVFormatter.encodeField(csv[i]));
			if (i < n - 1)
				sb.append(',');
		}

		return sb.toString();
	}
	
	public static String encodeRow(CSVRecord rec)
	{
		return encodeRow( rec.getFields() );
	}

}
