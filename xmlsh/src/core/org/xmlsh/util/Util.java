package org.xmlsh.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.XValue;

/**
 * @author DLEE
 *
 * Utility Functions
 */
public class Util
{
    private static DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat sDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static DateFormat sTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private static Random mRand = null;
 
	public static boolean isEmpty(String s)
	{
		return s == null || s.length() == 0;
	}
    
    
    public static boolean isBlank( String s)
    {
        if( s == null )
            return true;
        return isEmpty( s.trim() );
        
    }
    
    
    
    /*
	 * Copy an input to an output stream
	 */

	public static long copyStream(InputStream is, OutputStream os) throws IOException
	{
		byte[] buf = new byte[1024];
		int len;
		long size = 0;
		while ((len = is.read(buf)) > 0)
		{
			os.write(buf,0 , len);
			size += len;
		}

		return size;

	}

	/* 
	 * Read in input stream into a string
	 */

	public static byte[] readBytes(InputStream is) throws IOException
	{

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		@SuppressWarnings("unused") long size = copyStream(is, bs);
		return bs.toByteArray();

	}
	
	public static String readString(InputStream is) throws IOException
	{
		return new String( readBytes(is));
	}

	
	public static String readString(File file) throws IOException
	{
		return readString(new FileInputStream(file));
	}
	public static String replace(String str, String pattern, String replace)
	{
		if (replace == null)
		{
			replace = ""; //$NON-NLS-1$
		}
		int s = 0, e = 0;
		StringBuffer result = new StringBuffer();
		while ((e = str.indexOf(pattern, s)) >= 0)
		{
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		if (s == 0)
			return str;

		result.append(str.substring(s));
		return result.toString();
	}

    public static boolean isInt( String string , boolean sign )
    {
        for (int i = 0; i < string.length(); i++)
		{
            char c = string.charAt(i);
            if( sign && i == 0 ){
            	if( c == '+' || c == '-' )
            		continue;
            }
            
            if( ! Character.isDigit(c))
                return false ;			
		}
        return true ;
        
        
    }


	/**
	 * Method intValue.
	 * @param string
	 * @return int
	 */
	public static int parseInt(String string, int defValue)
	{
        if( isEmpty( string ) ) 
            return defValue ;
        
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			return defValue;
		}
	}

	/**
	 * Method trim.
	 * @param string
	 * @return String
	 */
	public static String trim(String string)
	{
		return string == null ? "" : string.trim(); //$NON-NLS-1$
	}
    
    public static String repeat( char c , int n )
    {
        StringBuffer sb = new StringBuffer(n);
        while( n-- > 0 )
            sb.append( c ) ;
        return sb.toString();
    }

	public static String pad(String str, int width, char pad, boolean bRight)
	{
		if (str == null)
			str = ""; //$NON-NLS-1$
		int len = str.length();
		// Blank pad to mWidth
		if (len < width)
		{
            if( bRight )
                str = repeat( pad , width - len ) + str ;
            else
                str = str + repeat( pad , width - len  ) ;
		}
		return str;
	}

	public static String pad(String str, int width)
	{
		return pad(str, width, ' ' , false );
	}

	public static String pad(int width)
	{
		return pad(null, width, ' ' , false );
	}

	public static String lineBreak(boolean bHtml)
	{
		return  bHtml ? "<br>\n" : "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isTrue(String sEnabled)
	{
        if( sEnabled == null )
            return false;
		return sEnabled.equalsIgnoreCase("true") || sEnabled.equalsIgnoreCase("yes"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    
    

	/**
	 * Method parseDouble.
	 * @param string
	 * @return double
	 */
	public static double parseDouble(String str, double defValue)
	{
		
        try
        {
            return Double.valueOf(str).doubleValue();
        }
        catch (Exception e)
        {
        }
        return defValue;
	}
    
    /**
     * Method parseDouble.
     * @param string
     * @return double
     */
    public static double parseDouble(String str)
    {
       return parseDouble( str , 0.) ;
    }
    
    
    public static boolean parseBoolean(String string)
    {
        if( isBlank(string))
            return false;
        
        if ("1".equals(string) || "true".equals(string))
            return true;

        return false;

    }
    
    
    public static String urlEncode( String value )
    {
        try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
            
            return value;
		}       
        
    }

	/**
     * String equality without crashing if null 
	 * @param string
	 * @param string2
	 * @return
	 */
	public static boolean isEqual(String string, String string2)
	{
        // both null/"" true
        if( isEmpty( string ) && isEmpty( string2 ))
            return true ;
        
        // either null/"" false
        if( isEmpty( string )|| isEmpty(string2))
            return false ;
        
        return string.equals(string2);            

	}

	

	/**
	 * @param message
	 * @param i
	 * @return
	 */
	public static String trim(String message, int i) {
        message = trim(message);
        if( message.length() > i )
            message  = message.substring(0,i);
        return message;
        
	}
    

	public static String toHex( byte b )
	{
	    String hex = Integer.toString((int)b&0xFF, 16);
	    if (hex.length() < 2)
	        hex = "0" + hex;
	    return hex;
	}
    

	public static String notNull(String str) {
		if( str == null ) return "";
		return str;
	}

	public static String formatMessage(Exception e) {
		String msg = e.getMessage();
		Throwable cause = e.getCause() ;
		if( cause != null )
			msg = msg + "\nCause: " + cause.getMessage();
		return msg;
	}

	public static TransformerHandler getTransformerHander(OutputStream stdout)
	throws TransformerFactoryConfigurationError, TransformerConfigurationException,
	IllegalArgumentException {
		return getTransformerHander( new StreamResult(stdout), "UTF-8");
	
	}
	
	
	public static TransformerHandler getTransformerHander(StreamResult streamResult, String encoding)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException,
			IllegalArgumentException {
	
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		TransformerHandler hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,encoding	);
		// serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
		hd.setResult(streamResult);
		return hd;
	}


	public static boolean isIdentifier(char c) {
		return  c == '_' ||Character.isLetter(c) || Character.isDigit(c);
	}

	/*
	 * Filename style wildcard matching 
	 */

	public static boolean wildMatches(String pattern, String word) {
		/* 
		 * Create a java regex that coresponds to pattern
		 */
		
		String reg = "^" + 
			pattern.
			replace(".", "\\.").
			replace("*", ".*").
			replace("?" , ".") + "$";
		
		// Special case, single "[" (test)
		if( reg.equals("^[$"))
			reg = "^\\[$";
		
		
		
		
		return word.matches(reg);
		
		
	}


	public static boolean hasAnyChar(String s, String any) {
		for( int i = 0 ; i < any.length() ; i++ ){
			char c = any.charAt(i);
			if( s.indexOf(c) >= 0 )
				return true ;
		}
		return false;
	}


	public static int parseInt(XValue value, int def ) {
		return parseInt( value.toString(), def );
	}


	public static List<XValue> toList(XValue[] args) {
		ArrayList<XValue> a = new ArrayList<XValue>();
		for(int i =0  ; i < args.length ; i++)
			a.add(args[i]);
		return a;
	}


	public static List<String> toStringList(List<XValue> a) {
		ArrayList<String> 	list = new ArrayList<String>();
		for( XValue v : a )
			list.add(v.toString());
		return list;
	}


	public static String readLine(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();
		int c;
		while( (c=is.read()) > 0 && c != '\n' ){
			if( c != '\r' )
				sb.append((char)c);
		}
		return sb.toString();
	}


	public static List<XValue> expandSequences(List<XValue> values)
	{
		ArrayList<XValue> list = new ArrayList<XValue>( values.size());
		for( XValue arg : values ){
			if( arg.isString() )
				list.add(arg);
			else {
				XdmValue xv = arg.toXdmValue();
				Iterator<XdmItem> iter = xv.iterator();
				while( iter.hasNext() )
					list.add( new XValue( iter.next() ));
			}
		}
		return list;
	}
}

//
//
//Copyright (C) 2008, David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
