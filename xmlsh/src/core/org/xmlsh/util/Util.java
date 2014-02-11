package org.xmlsh.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
/**
 * @author DLEE
 *
 * Utility Functions
 */
public class Util
{
	private static final String sXSDT_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss";
	public static byte mNewline[];
	private static Pattern mURIPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\.+-]+:.*");
	
  
	
	private static class FileComparator implements Comparator<File>
	{

		public int compare(File o1, File o2) {
			return toJavaPath(o1.getName()).compareTo(toJavaPath(o2.getName()));
		}
		
	}
	
	
	
	
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
	
	public static String readString(InputStream is, String encoding) throws IOException
	{
		return new String( readBytes(is), encoding );
	}



	public static String readString(URL url, String encoding ) throws IOException
	{
		InputStream is = url.openStream();
		String ret = new String( readBytes(is));
		is.close();
		return ret;
	}
	public static String readString(File file, String encoding ) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		String ret = readString(fis, encoding );
		fis.close();
		return ret;
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
	
	public static long parseLong(String string, long defValue)
	{
        if( isEmpty( string ) ) 
            return defValue ;
        
		try
		{
			return Long.parseLong(string);
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
	public static boolean isEqual(String string1, String string2)
	{
		// DAL: 2010-11-10  Optimize out multiple calls to isEmpty
		
		boolean bIsEmpty1 = (string1 == null || string1.length() == 0 );
		boolean bIsEmpty2 = (string2 == null || string2.length() == 0 );
		
		
		
        // both null/"" true
        if( bIsEmpty1 && bIsEmpty2 )
            return true ;
        
        // either null/"" but not both = false 
        if( bIsEmpty1 || bIsEmpty2 )
            return false ;
        
        return string1.equals(string2);            

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

	public	static String	nullIfBlank( String str )
	{
		return isBlank(str) ? null : str.trim(); 
		
	}
	
	
	public static String formatMessage(Exception e) {
		String msg = e.getMessage();
		Throwable cause = e.getCause() ;
		if( cause != null )
			msg = msg + "\nCause: " + cause.getMessage();
		return msg;
	}

	public static TransformerHandler getTransformerHander(OutputStream stdout,SerializeOpts opts)
	throws TransformerFactoryConfigurationError, TransformerConfigurationException,
	IllegalArgumentException {
		return getTransformerHander( new StreamResult(stdout),opts);
	
	}
	

	
	
	public static TransformerHandler getTransformerHander(Result result, SerializeOpts opts )
			throws TransformerFactoryConfigurationError, TransformerConfigurationException,
			IllegalArgumentException {
	
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		tf.setAttribute(FeatureKeys.CONFIGURATION, Shell.getProcessor().getUnderlyingConfiguration());
		
		
		
		// SAX2.0 ContentHandler.
		TransformerHandler hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, opts.getOutputXmlEncoding()	);
		// serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT, opts.isIndent() ? "yes" : "no");
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, opts.isOmit_xml_declaration() ? "yes" : "no");
		hd.setResult(result);
		return hd;
	}


	public static boolean isIdentifier(char c) {
		return  c == '_' ||Character.isLetter(c) || Character.isDigit(c);
	}

	/*
	 * Filename style wildcard matching 
	 */

	public static boolean wildMatches(String pattern, String word, boolean caseSensitive ) {
		/* 
		 * Create a java regex that coresponds to pattern
		 */
		
		String reg = "^" + 
			pattern.
			replace("^","\\^").
			replace("+","\\+").
			replace(".", "\\.").
			replace("*", ".*").
			replace("?" , ".").
			replace("(","\\(").
			replace(")","\\)") + "$";
		
		// Special case, single "[" (test)
		if( reg.equals("^[$"))
			reg = "^\\[$";
		
		Pattern p = Pattern.compile(reg, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
		return p.matcher(word).matches();
		
		
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
	
	public static List<String> toList(String[] args)
	{
		ArrayList<String> a = new ArrayList<String>();
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
	public static String[] toStringArray(List<XValue> a) {
		List<String> list = toStringList(a);
		return list.toArray( new String[list.size() ] );
	}


	public static String readLine(InputStream is, String encoding) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int c;
		
		
		boolean bAny = false ;
		while( (c=is.read()) > 0 && c != '\n' ){
			bAny = true;
			if( c != '\r' )
				bos.write( c);
		}
		if( c == -1 && ! bAny )
			return null;
		bos.close();
		return bos.toString(encoding);
	}


	public static List<XValue> expandSequences(List<XValue> values)
	{
		/*
		 * Avoid making a new list if this list is a single element
		 */
		if( values == null || values.isEmpty() )
			return values ;
	
		
		ArrayList<XValue> list = new ArrayList<XValue>( values.size());
		for( XValue arg : values ){
			if( arg == null  )
				continue ;
			if( arg.isAtomic() || arg.isObject() )
				list.add(arg);
			else {
				XdmValue xv = arg.asXdmValue();
				Iterator<XdmItem> iter = xv.iterator();
				while( iter.hasNext() )
					list.add( new XValue( iter.next() ));
			}
		}
		return list;
	}


	public static synchronized byte[] getNewline(SerializeOpts opts)
	{
		if( Util.mNewline == null ){
			try {
				Util.mNewline = System.getProperty("line.separator").getBytes(opts.getOutputTextEncoding());
			} catch (UnsupportedEncodingException e) {
				Util.mNewline = new byte[] { '\n' };
			} 
		}
		return Util.mNewline;
	}

	public static void sortFiles( File[] list )
	{
		Arrays.sort(list, new FileComparator() );
	}
	
	public static void sortFiles( List<File> list )
	{
		Collections.sort(list, new FileComparator() );
	}
	
	/**
	 * Convert a Path or name in DOS format to Java format
	 * This means converting \ to / 
	 */
	
	public static String toJavaPath( String path )
	{
		if( path == null )
			return null;
		if( File.separatorChar != '/')
			return path.replace(File.separatorChar, '/');
		else
			return path;
	}

	/*
	 * Reverse the conversion of toJavaPath
	 */
	public static String fromJavaPath( String path )
	{
		if( path == null )
			return null;
		if( File.separatorChar != '/')
			return path.replace('/' , File.separatorChar);
		else
			return path;
	}

	public static String readLine(Reader ir) throws IOException {
		StringBuffer sb = new StringBuffer();
		int c;
		boolean bAny = false ;
		while( (c=ir.read()) > 0 && c != '\n' ){
			bAny = true;
			if( c != '\r' )
				sb.append((char)c);
		}
		if( c == -1 && ! bAny )
			return null;
		
		return sb.toString();
	}


	public static  ByteArrayInputStream toInputStream(String script,SerializeOpts opts) throws UnsupportedEncodingException {
			return new ByteArrayInputStream(
					script.getBytes(opts.getInputTextEncoding()));
	}


	public static void writeXdmValue(XdmValue value, Destination destination) throws SaxonApiException 
    {
        for (Iterator<XdmItem> it = value.iterator(); it.hasNext();) {
            XdmItem item = it.next();
            writeXdmItem(item, destination );
        }
	}
	public static void writeXdmItem(XdmItem item, Destination destination) throws SaxonApiException {
	    try {
	    	
	    	if( item instanceof XdmNode ){
				XdmNode node = (XdmNode) item ;
				if( node.getNodeKind() == XdmNodeKind.ATTRIBUTE )
					item = new XdmAtomicValue( node.getStringValue());
				
			}
	    	
	    	
	    	
	        Receiver out = destination.getReceiver(Shell.getProcessor().getUnderlyingConfiguration());
	        
	        // DAL: 2010-10-15 - 
	        // Added in namespace reducer in order to filter out duplicate and redundant namespaces
	        out = new NamespaceReducer(out);
	        
	        
	        
	        ComplexContentOutputter out2 = new ComplexContentOutputter(Shell.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration());
	       
	        out2.setReceiver(out);
	  
		    TreeReceiver tree = new TreeReceiver(out2);
	        tree.open();
	        tree.startDocument(0);
	        tree.append((Item)item.getUnderlyingValue(), 0, NodeInfo.LOCAL_NAMESPACES ); // NodeInfo.NO_NAMESPACES);//NodeInfo.ALL_NAMESPACES);
	        
	        tree.endDocument();
	        tree.close();
	      
	        
	    } catch (XPathException err) {
	        throw new SaxonApiException(err);
	    }
	}

	/*
	 * Try to create a URI from a string and return null instead of exception on failure
	 * 
	 */
	public static URI tryURI( String s )
	{
		// First check for a-z{2}+
		
		

		URI uri = null;
        Matcher m = mURIPattern.matcher(s);
        if(  m.matches() )
			try {
				uri = new URI(s);
			} catch (URISyntaxException e) {
				return null;
			}
        
			return uri ;
		
		
	}
	
	public static URL tryURL( String s )
	{
		URI uri = tryURI( s );
		if( uri != null )
			try {
				return uri.toURL();
			} catch (MalformedURLException e) {
				return null ;
			}
		
		return null;
		
	}
	
	
	
	/*
	
	public static boolean isURIScheme(String file) {
		
		return file.startsWith("http:") ||
				file.startsWith("https:") ||
				file.startsWith("ftp:") ||
				file.startsWith("file:") ||
				file.startsWith("jar:");
				
		
		return file.matches("[a-z][a-z]+:.*");
	}

	*/

	public static String readString(URI uri, String encoding) throws MalformedURLException, IOException {
		return readString( uri.toURL(),encoding);
	}


	public static Destination streamToDestination(OutputStream out, SerializeOpts opts) {
		
		Serializer dest = getSerializer(opts);
		dest.setOutputStream(out);	
		return dest;
	}
	
	
	public static Serializer getSerializer(SerializeOpts opts) {
		
		Serializer ser = Shell.getProcessor().newSerializer();
		ser.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, 
				opts.isOmit_xml_declaration() ? "yes" : "no");
		ser.setOutputProperty(Serializer.Property.INDENT , opts.isIndent() ? "yes" : "no");
		// dest.setOutputProperty(Serializer.Property.VERSION,"1.1");
		
		ser.setOutputProperty(Serializer.Property.METHOD, opts.getMethod() );
		ser.setOutputProperty(Serializer.Property.ENCODING, opts.getOutputXmlEncoding());

		return ser;
	}
	
	
	public static boolean isWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}


	


	public static XdmNode asXdmNode(URL url) throws IOException, SaxonApiException {

		InputStream is = url.openStream();
		try {
		
			Source s = new StreamSource(is);
			s.setSystemId(url.toExternalForm());
			
			
			net.sf.saxon.s9api.DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
			
			return builder.build(s);
		} finally {
			is.close();
		}
	}

	// Format as xs:datetime

	public static String formatXSDateTime(Date date) {
		// YYYY-MM-DDThh:mm:ss
		return (new SimpleDateFormat(sXSDT_FORMAT_STR)).format(date);
	}


	public static void safeClose(InputStream is) {
		try {
			if( is != null )
				is.close();
		} catch( Exception e )
		{
			
		}
		
	}
	
	
	 /**
	 * DAL: NOTE: Fixed version of the Saxon S9API function of the same name in QName
	 * Saxon version truncates the namespaceURI by 1 letter
	 * 
     * Factory method to construct a QName from a string containing the expanded
     * QName in Clark notation, that is, <code>{uri}local</code>
     * <p/>
     * The prefix part of the <code>QName</code> will be set to an empty string.
     * </p>
     *
     * @param expandedName      The URI in Clark notation: <code>{uri}local</code> if the
     *                          name is in a namespace, or simply <code>local</code> if not.
     * @return the QName corresponding to the supplied name in Clark notation. This will always
     * have an empty prefix.
     */

    public static QName qnameFromClarkName(String expandedName) {
        String namespaceURI;
        String localName;
        if (expandedName.charAt(0) == '{') {
            int closeBrace = expandedName.indexOf('}');
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in Clark name");
            }
            namespaceURI = expandedName.substring(1, closeBrace /* DAL: FIX: WAS: -1 */ );
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in Clark name");
            }
            localName = expandedName.substring(closeBrace + 1);
        } else {
            namespaceURI = "";
            localName = expandedName;
        }

        return new QName("", namespaceURI, localName);
    }

    /*
     * Resolve a string as a QName
     * May be 
     * 	local
     * 	prefix:local
     *  {clarknotation}local
     *  
     *  Only if prefix:is used is the ns used
     *  
     */

    
	public static QName resolveQName(String name, Namespaces ns) {
		if( name.startsWith("{"))
			return qnameFromClarkName(name);
		
		int colon = name.indexOf(':');
		if( colon < 0 )
			return new QName( name );
		
		String prefix = name.substring(0, colon);
        String local = name.substring(colon + 1);
        String uri = "";
        if( prefix.length() > 0 )
        	uri = ns.get(prefix);
        if( uri == null ) uri = "";
        
        return new QName( prefix , uri , local );
		
		
		
		
	}
	
	
	private static void copyFile(File src, File dest, boolean force) throws IOException {

		// Try copy 
		InputStream in = null;
		OutputStream out = null;
		try {
		
			in = new FileInputStream(src);
			
			// Try deleting dest if we have to
			if( force && dest.exists() && ! dest.canWrite() )
				dest.delete();
			
			out = new FileOutputStream(dest);
			Util.copyStream( in , out );
		} finally {
			if( in != null ) in.close();
			if( out != null ) out.close();
		}
	}

	
	/**
	 * Move a file, possibly renaming it
	 * 
	 * @param inFile
	 * @param file
	 */

	public static void moveFile(File src, File dest, boolean force) throws IOException {
		if( dest.exists() && force)
			dest.delete();
		
		
		
		
		// Simple rename
		if( src.renameTo(dest))
			return ;
		
		copyFile(src,dest,force);
		src.delete();
		
	}


	/*
	 * Return true if variable is PATH or XPATH
	 * On Windows env variables are case InSensitive 
	 * 
	 */
	public static boolean isPath(String var)
	{ 
		if( isWindows())
			return var.equalsIgnoreCase("PATH")||var.equalsIgnoreCase("XPATH");
		else	
			return var.equals("PATH")||var.equals("XPATH");

	
	}


	public static List<XValue> combineSequence(List<XValue> result) {
		if( result.size() < 2 )
			return result ;
		
		XValue value = new XValue(result);
		
		List<XValue> v = new ArrayList<XValue>(1);
		v.add(value);
		return v;
		
		
	}


	// Format time as xs:datetime
	public static String formatXSDateTime(long lastModified) 
	{
		Date date = new Date(lastModified);
		return formatXSDateTime( date );
		
	
		
	}
	

	public static String convertPath(String name, boolean bSystem) {
		if( bSystem && File.separatorChar != '/')
			return name.replace('/', File.separatorChar);
		else
			return name.replace(File.separatorChar, '/');
			
	}


	public static List<XValue> toXValueList(String[] args) {
		List<XValue> list = new ArrayList<XValue>(args.length);
		for( String a : args )
			list.add( new XValue(a) );
		return list ;
	}
		


	public static int  fromHexChars( char[] chars , int i )
	{
		
		
		int n1 = chars[i] >= 'A' ? (10 + (chars[i] - 'A')) : ( chars[i] - '0');
		i++;
		int n2 = chars[i] >= 'A' ? (10 + (chars[i] - 'A')) : ( chars[i] - '0');
		byte b = (byte)( (n1 << 4) | n2 );
		return b & 0xFF;
		
	}
	
	public static void toHexByte( byte b, StringBuffer sb ) {
			int n1 = (b & 0xF0 ) >> 4 ;
			int n2 = (b & 0xF ) ;
			sb.append((char) ( n1 < 10 ? n1 + '0' : (n1 - 10)  + 'A' ) );
			sb.append( (char) (n2 < 10 ? n2 + '0' : (n2 - 10)  + 'A' ) );
		
	}
	

	public static String toHex(byte[] data) {
		StringBuffer sb = new StringBuffer( data.length * 2 );
		for(byte b : data )
		    toHexByte( b , sb );
		return sb.toString();
		
	}
	public static void toHexChar( char ch, StringBuffer sb )
	{
		if( (ch & 0xFF00) != 0 )
			toHexByte( (byte) ((ch >> 8 ) & 0xFF), sb  );
		toHexByte( (byte) (ch & 0xFF), sb  );
		
		
	}
	// Simplified compatible versions of xdmp:encode-for-NCName and xdmp:decode-from-NCName
	public static String encodeForNCName( String name )
	{
		StringBuffer sb = new StringBuffer( name.length() * 2 );
		char[] chars = name.toCharArray();
		
		boolean bFirst = true ;
		boolean escaped = false ;
		for( char ch : chars ){
			if( ch == '_') {
				sb.append("__");
				escaped = true ;
			} 
			else 
			if( ch  == ':' || 
				(bFirst? !isInitialNameChar(ch) : 
				!isNameChar(ch))){
				  sb.append('_');
			      toHexChar( ch , sb );
				  sb.append('_');
			      escaped = true;

			}
		    else
			     sb.append(ch);
			bFirst = false ;
		}
		if( sb.length() == 0 )
			  sb.append('_');
		else
		if( ! escaped ) 
			return name; 

		return sb.toString();
		
	}
	
	public static String decodeFromNCName( String name )
	{

		  StringBuffer sb = new StringBuffer( name.length() * 2 );
		  boolean escaped = false;

          char chars[] = name.toCharArray() ;

 		  for( int i = 0 ; i < chars.length ; i++ )
 		  {
 			char ch = chars[i];
 			
		    if( ch == '_' ){
		      escaped = true ;
		      char c = 0;
		      i++;
		 
		      while(  i < chars.length-1  ){
		    	if( chars[i] == '_' ){
		    		break ;
		    	}
			    c <<= 8;
		    	c |= fromHexChars( chars , i );
		    	i +=2 ;
	

		      } 

		      if( c == 0 )
		        sb.append('_');
		      else
		        sb.append( c );



		    } else  
		    	sb.append(ch);
 		  }
 		  if( ! escaped )
 			  return name ;
 		  return sb.toString();
	}

	public  static boolean isNameChar(char ch) {
		
		  if ((ch=='-')|(ch=='.')|(ch==':')) return true;
		  if (ch<'0') return false;
		  if (ch<='9') return true;
		  if (ch<'A') return false;
		  if (ch<='Z') return true;
		  if (ch=='_') return true;
		  if (ch<'a') return false;
		  if (ch<='z') return true;
		  if (ch==0xb7) return true;
		  if (ch<0xc0) return false;
		  if (ch<=0xd6) return true;
		  if (ch<0xd8) return false;
		  if (ch<=0xf6) return true;
		  if (ch<0xf8) return false;
		  if (ch<=0x37d) return true;
		  if (ch<0x37f) return false;
		  if (ch<=0x1fff) return true;
		  if (ch<0x200c) return false;
		  if (ch<=0x200d) return true;
		  if (ch<0x203f) return false;
		  if (ch<=0x2040) return true;
		  if (ch<0x2070) return false;
		  if (ch<=0x218f) return true;
		  if (ch<0x2c00) return false;
		  if (ch<=0x2fef) return true;
		  if (ch<0x3001) return false;
		  if (ch<=0xd7ff) return true;
		  if (ch<0xf900) return false;
		  if (ch<=0xfdcf) return true;
		  if (ch<0xfdf0) return false;
		  if (ch<=0xfffd) return true;
		  return false; 
		
	}

	public  static boolean isInitialNameChar(char ch) {
		 if (ch==':') return true;
		  if (ch<'A') return false;
		  if (ch<='Z') return true;
		  if (ch=='_') return true;
		  if (ch<'a') return false;
		  if (ch<='z') return true;
		  if (ch<0xc0) return false;
		  if (ch<=0xd6) return true;
		  if (ch<0xd8) return false;
		  if (ch<=0xf6) return true;
		  if (ch<0xf8) return false;
		  if (ch<=0x2ff) return true;
		  if (ch<0x370) return false;
		  if (ch<=0x37d) return true;
		  if (ch<0x37f) return false;
		  if (ch<=0x1fff) return true;
		  if (ch<0x200c) return false;
		  if (ch<=0x200d) return true;
		  if (ch<0x2070) return false;
		  if (ch<=0x218f) return true;
		  if (ch<0x2c00) return false;
		  if (ch<=0x2fef) return true;
		  if (ch<0x3001) return false;
		  if (ch<=0xd7ff) return true;
		  if (ch<0xf900) return false;
		  if (ch<=0xfdcf) return true;
		  if (ch<0xfdf0) return false;
		  if (ch<=0xfffd) return true;
		  return false;
	
	
	
	}


	public static boolean hasDirectory(String name) {
		File f = new File(name);
		return f.getParent() != null ;
	}


	public static String readString(File file, SerializeOpts serializeOpts) throws IOException {
		InputStream is = new FileInputStream(file);
		String s = readString( is , serializeOpts.getInput_text_encoding());
		is.close();
		return s;
	}


}

//
//
//Copyright (C) 2008-2014    David A. Lee.
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
