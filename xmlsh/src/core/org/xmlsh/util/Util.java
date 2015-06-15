package org.xmlsh.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
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
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;

import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IReleasable;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.sh.shell.CharAttr;
import org.xmlsh.sh.shell.CharAttrs;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.ITypeConverter;
/**
 * @author DLEE
 *
 * Utility Functions
 */
public class Util
{
	private static final String sXSDT_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss";
	public static volatile  byte mNewLineBytes[];
	private static Pattern mURIPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\.+-]+:.*");
	private static  volatile   String mNewlineString;
	private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger( Util.class);



	private static class FileComparator implements Comparator<File>
	{

		@Override
		public int compare(File o1, File o2) {
			return FileUtils.toJavaPath(o1.getName()).compareTo(FileUtils.toJavaPath(o2.getName()));
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
		 String ret = new String( readBytes(is) , encoding );
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

			 mLogger.info("Exception parsing double: " + str,e );
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
		 return parseBoolean(string,false);

	 }

	 public static boolean parseBoolean(String string, boolean def)
	 { 

		 if( isBlank(string))
			 return def;

		 if ("1".equals(string) || "true".equals(string))
			 return true;

		 return def;
	 }

	 public static String urlEncode( String value )
	 {
		 try
		 {
			 return URLEncoder.encode(value, ShellConstants.kENCODING_UTF_8);
		 }
		 catch (UnsupportedEncodingException e)
		 {
			 mLogger.info("Exception encoding URL to UTF-8: " +  value ,e );

			 return value;
		 }       

	 }

	 /**
	  * String equality without crashing if null 
	  * @param string
	  * @param string2
	  * @return
	  */

	 public static boolean isEqual(String string1, String string2 ){
	     return isEqual(string1,string2,false);
	 }

	 public static boolean isEqual(String string1, String string2,boolean ignCase)
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

		 return ignCase ? string1.equalsIgnoreCase(string2) : string1.equals(string2);            

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
		 String hex = Integer.toString(b&0xFF, 16);
		 if (hex.length() < 2)
			 hex = "0" + hex;
		 return hex;
	 }


	 public static String notNull(String str) {
		 if( str == null ) return "";
		 return str;
	 }

	 public static boolean notEmpty(String str) {

	     return ! isEmpty(str) ;
	 }
	 public static boolean  notBlank(String str) {
	      return ! isBlank(str) ;

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

		 SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
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
	 * Wrapper over NIO PathMatcher
	 */

	 public static PathMatcher getPathMatcher( String pattern ) {
		 
		 try {
		 return
		 FileSystems.getDefault().getPathMatcher( "glob:" + pattern );
		 } catch(  IllegalArgumentException |  UnsupportedOperationException  e ) {
				return null;
		 }
		 
	 }
	 
	
	 
	 /*
	  * Filename style wildcard matching - ONLY for non filenames 
	  * 
	  * Use getPathMatcher instead for real files - or maybe not ...
	  */

	 public static boolean wildMatches(String pattern, String word, boolean caseSensitive ) {
		 /* 
		  * Create a java regex that coresponds to pattern
		  */

		 return compileWild(pattern,caseSensitive).matcher(word).matches();
	 }
	 public static boolean containsWild( CharAttributeBuffer pattern ){
			 
		 int inbrack  = 0;
			 byte[] attrs = pattern.getAttrArray();
			 char[] chars = pattern.getCharArray() ;
			 // ANY of the char attrs prevents wild expansion  

			 for( int i = 0 ; i < chars.length ; i++ ){
				 if( attrs[i] != 0 ){
					 continue ;
				 }
				 
				 switch( chars[i]) {
				 case '*' :
				 case '?' :
					 return true ;
				 case '[' :
					 inbrack++;
					 break;
				 case ']' :
					 if( inbrack > 0 )
						 return true ;
				 }

			 }
			 return false ;
		 
	 }
	 
	 
	 public static Pattern compileWild( CharAttributeBuffer pattern ,  boolean caseSensitive) {
		 
		 StringBuilder sb = new StringBuilder();
		 StringBuilder literal = new StringBuilder();
		 
		 byte[] attrs = pattern.getAttrArray();
		 char[] chars = pattern.getCharArray() ;
		 
		 byte escape = CharAttr.ATTR_ESCAPED.toBit() ;

		 for( int i = 0 ; i < chars.length ; i++ ){
			 if( (attrs[i] & escape) == escape )
				 literal.append( '\\');
			 // ANY of the char attrs prevents wild expansion  
			 if( attrs[i] != 0 ){
				 literal.append( chars[i]);
				 continue ;
			 }
			 
			 String wild = null ;
			 switch( chars[i]) {
			 case '*' :
				 wild = ".*";
				 break ;
			 case '?' :
				 wild = ".";
				 break ;
			 case '[' :
				 int p = i+1;
				 while( p < chars.length  && chars[p] != ']' ){
					 p++;
				 }
				 if( p < chars.length ){
					 wild = pattern.subsequence(i, p+1).toString().toString();
					 i = p;
					 break ;
				 }

				 // Fall through
		     default:
		    	 literal.append( chars[i]);
		    	 continue;
			 }
			 if( literal.length() > 0){ 
				 sb.append( Pattern.quote(literal.toString()));
				 literal.setLength(0);
			 }
			 if( wild != null )
			   sb.append(wild);
		 }
		 if( literal.length() > 0) 
			 sb.append( Pattern.quote(literal.toString()));

		 return Pattern.compile(sb.toString() ,  caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
		 
	 }
		 
	 
	 
	 public static Pattern compileWild( String pattern,  boolean caseSensitive) {

		 String reg = "^" + 
				 pattern.
				 replace("^","\\^").
				 replace("+","\\+").
				 replace(".", "\\.").
				 replace("*", ".*").
				 replace("?" , ".").
				 replace("(","\\(").
				 replace(")","\\)"). 
				 replace("}", "\\}").
				 replace("{","/{")+ "$";

		 // Special case, single "[" (test)
		 if( reg.equals("^[$"))
			 reg = "^\\[$";

		 return Pattern.compile(reg, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );


	 }
	


		public static PathMatcher compileWild(
				FileSystem fileSystem, CharAttributeBuffer wild, CharAttrs escapeAttrs,
				boolean caseSensitive) {
			
		    String wildstr = wild.decodeString();

			try {
				return fileSystem.getPathMatcher("glob:" + wildstr  );
			} catch (PatternSyntaxException e) {
				mLogger.trace("Invalid glob expansion: {}" , wildstr , e );
			}
			return null;
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
			   for( XValue v : arg )
			     list.add( v );
		 }
		 return list;
	 }


	 public static byte[] getNewlineBytes(SerializeOpts opts)
	 {
		 if( Util.mNewLineBytes == null ){
			 try {
				 Util.mNewLineBytes = System.getProperty("line.separator").getBytes(opts.getOutputTextEncoding());
			 } catch (UnsupportedEncodingException e) {
				 if( isWindows() )
					 Util.mNewLineBytes = new byte[] { '\r','\n' };

				 else
					 Util.mNewLineBytes = new byte[] { '\n' };
			 } 
		 }
		 return Util.mNewLineBytes;
	 }

	 public static  String getNewlineString()
	 {
		 if( Util.mNewlineString == null )
			 Util.mNewlineString = System.getProperty("line.separator");
		 if( Util.mNewlineString == null ){
			 if( isWindows() )
				 Util.mNewlineString = "\r\n";
			 else
				 Util.mNewlineString  = "\n";
		 }
		 return Util.mNewlineString;
	 }

	 public static void sortFiles( File[] list )
	 {
		 Arrays.sort(list, new FileComparator() );
	 }

	 public static void sortFiles( List<File> list )
	 {
		 Collections.sort(list, new FileComparator() );
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

	 


	 public static  InputStream toInputStream(String script,SerializeOpts opts) throws UnsupportedEncodingException {
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
				 // Really ignore this
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
	
	 public static boolean isOSX() {
		    String osName = System.getProperty("os.name");
		    return osName.contains("OS X");
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



	 public static void safeClose(AutoCloseable closable) {
		 try {
			 if( closable != null )
				 closable.close();
		 } catch( Exception e )
		 {
			 mLogger.info("Exception closing: " + closable.getClass().getName() ,e);

		 }

	 }

	 public static void safeClose(XMLStreamWriter out) {
		 try {
			 if( out != null )
				 out.close();
		 } catch( Exception e )
		 {
			 mLogger.debug("Exception closing XMLStreamWriter",e);

		 }

	 }

	 public static void safeClose(XMLStreamReader reader) {
		 try {
			 if( reader != null )
				 reader.close();
		 } catch( Exception e )
		 {
			 mLogger.debug("Exception closing  XMLStreamReader",e);

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

		 return new QName( namespaceURI, localName, "");
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

		 return new QName(  uri , local , prefix );




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
			 return var.equalsIgnoreCase(ShellConstants.PATH)||var.equalsIgnoreCase(ShellConstants.ENV_XPATH);
		 else	
			 return var.equals(ShellConstants.PATH)||var.equals(ShellConstants.ENV_XPATH);


	 }

	 /*
	  * Convert a List of XValues into a List with 1 XValue 
	  */

	 public static List<XValue> combineSequence(List<XValue> result) {
	     if( result == null )
	         return XValue.emptyList();
	     
		 if( result.size() < 2 )
			 return result ;

		 XValue value = XValue.newXValue(result);

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


	 public static List<XValue> toXValueList(String[] args) {
		 List<XValue> list = new ArrayList<XValue>(args.length);
		 for( String a : args )
			 list.add( XValue.newXValue(a) );
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
	 
	 public static QName encodeForQName( String name )
	 {
	     
	     if( !name.contains("::"))
	         return new QName( encodeForNCName(name ) );
	     String ns[] = name.split("::");
	     // TODO: Lookup actual namespace
	     return new QName( encodeForNCName(ns[0]) , encodeForNCName(ns[1]) , encodeForNCName(ns[0]) );
	     
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
	 public static String  decodeFromQName( QName qname ){
	     
	     if( Util.isBlank(qname.getPrefix()) )
             return decodeFromNCName(qname.getLocalPart()) ;
	     else
	         return qname.getPrefix() + "::" + decodeFromNCName(qname.getLocalPart());

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


	 public static String readString(File file, SerializeOpts serializeOpts) throws IOException {
		 InputStream is = new FileInputStream(file);
		 String s = readString( is , serializeOpts.getInput_text_encoding());
		 is.close();
		 return s;
	 }

	 public static <T> String stringJoin(T[] list, String sep) {
		 if( list == null ) return "()";
		 
		 StringBuilder sb = new StringBuilder();

		 for( T s : list ){
			 if(sb.length() > 0 )
				 sb.append(sep);
			 sb.append(s.toString());
		 }
		 return sb.toString();
	 
	 }


	 public static String stringJoin(List<String> list, String sep) {
		 if( list == null)
			 return null ;

		 StringBuilder sb = new StringBuilder();

		 for( String s : list ){
			 if(sb.length() > 0 )
				 sb.append(sep);
			 sb.append(s);
		 }
		 return sb.toString();

	 }


	 public static boolean isEmpty(Collection<?> list)
	 {
		 return list == null  || list.isEmpty();

	 }


	 public static String removeTrailingNewlines(String s, boolean ignorCR)
	 {

		 if( s == null )
			 return null ;

		 int len = s.length();

		 int end = len-1;
		 while( end >= 0 && isNewline( s.charAt(end) , ignorCR ) )
			 end--;
		 if( end < len-1 )
			 return s.substring(0 , end+1 );
		 else
			 return s;



	 }


	 private static boolean isNewline(char c, boolean ignorCR)
	 {
		 return c == '\n' || (ignorCR && c == '\r');
	 }

	 // Skip to c and return prefix, return null if c is never encountered before EOF
	 public static String skipToByte(InputStream is, int delim ) throws IOException {
		 if( is == null )
			 return null ;
		 StringBuffer sb = new StringBuffer();
		 int c;
		 while( ( c = is.read() ) >= 0 ) {
			 if( c == delim )
				 return sb.toString();
			 sb.append((byte)c);
		 }
		 return null;

	 }


	 public static void safeRelease(IReleasable p)
	 {
		 if( p != null ) {
			 try {
				 p.release();
			 } catch( Exception e ) {
				 mLogger.warn("Exception closing: " + p.getClass().getName() ,  e );
			 }
		 }
	 }


	 public static void safeClose(XMLEventReader reader)
	 {
		 if( reader != null )
			 try {
				 reader.close();
			 } catch (XMLStreamException e) {
				 mLogger.warn("Exception closing reader" ,  e );
			 }

	 }


	 @SuppressWarnings("unchecked")
	 public static <T extends Throwable> void wrapException(Throwable e , Class<T> cls ) throws T
	 {
		 if( e.getClass().isInstance(cls) )
			 throw (T) e ;
		 T ex  = null ;
		 try {
			 ex = JavaUtils.newObject(cls, e );
		 } catch (Exception e1) {
			 String msg = "Exception wrapping exception type: " + e.getClass().getName() + " to class: " + cls.getName() ;
			 mLogger.warn( msg, e1 );
			 throw new IllegalArgumentException( msg + e1.getMessage() , e );
		 } 
		 throw ex ;


	 }

	 @SuppressWarnings("unchecked")
	 public static <T extends Throwable> void wrapException( String message,Throwable e, Class<T> cls ) throws T
	 {
		 // Need to add the message
		 // if( e.getClass().isInstance(cls) )
		 //			throw (T) e. ;
		 T ex  = null ;
		 try {
			 ex = JavaUtils.newObject(cls, message ,  e );
		 } catch (Exception e1) {
			 String msg = "Exception wrapping exception type: " + e.getClass().getName() + " to class: " + cls.getName() ;
			 mLogger.warn( msg, e1 );
			 throw new IllegalArgumentException( message + ": " + msg + e1.getMessage() , e );
		 } 
		 throw ex ;


	 }

	 /*
	  * Come up with a simple name from a complex one like a command 
	  */

	 public static String simpleName(String command,String def)
	 {
		 // Tokenize into words including path chars
		 String words[] = command.split("[^\\w]+");
		 for( String w : words ) {
			 String name = FileUtils.basePathLikeName(w);
			 // Convert paths java paths but dont use the path functions - might not really be a path

			 if( isBlank(name) || name.length() < 2 )
				 continue ;
			 // Trip off leading parts if looks like a path
			 if( name.length() > 10 )
				 name = name.substring(0,10) + "...";
			 return name ;
		 }

		 return def;

	 }



	 /*
	  * EnumSet helpers
	  */
	 public static <T extends Enum<T>> boolean setContainsAll( EnumSet<T> set , T...  items ) 
	 {
		 if( items.length == 0 )
			 return true ;
		 
		 for( T item : items )
			 // null item is ignored
			 if( item != null  && ! set.contains(item ))
				 return false;
		 return true ;
	 }

	 public static <T extends Enum<T>> boolean setContainsAny( Set<T> set , T...  items ) 
	 {
		 if( set.size() == 0 || items.length == 0  )
			 return false ;
		 for( T e : items )
			 if( e != null &&  set.contains(e))
				 return true ;
		 return false ;
	 }

	 public static <T extends Enum<T>> boolean setContainsAny( Set<T> set , Set<T> items) 
	 {		
		 if( set.size() == 0  )
			 return false ;
		 if( items == null || items.size() == 0 )
			 return false ;
		 
		 for( T e : items )
			 if( e != null && set.contains(e))
				 return true ;
		 return false ;

	 }


	 public static  <T extends Enum<T>> EnumSet<T> enumSetOf( T e , T... enums )
	 {
		 EnumSet<T> set = EnumSet.of(e);
		 if( enums.length > 0 )
			 for( T v : enums ) {
				 if( v != null )
  				 set.add(v);
			 }
		 return set ;
	 }




	 public static <T extends Enum<T>> EnumSet<T> withEnumAdded(EnumSet<T> set, T on)
	 {
		 if( on == null || set.contains(on))
			 return set;
		 set = set.clone();
		 set.add(on);
		 return set ;
	 }

	 public static <T extends Enum<T>> EnumSet<T> withEnumsAdded(EnumSet<T> set, EnumSet<T> on)
	 {
		 if( on.isEmpty() )
			 return set ;
		 if( set.containsAll(on))
			 return set;
		 set = set.clone();
		 set.addAll( on );
		 return set ;
	 }
	 public static <T extends Enum<T>> EnumSet<T> withEnumsAdded(EnumSet<T> set, T... on )
	 {
		 if(  on.length == 0 )
			 return set ;
		 
		 set = set.clone();
		 for( T v : on ) 
			if( v != null)
			   set.add(v);

		 return set;
	 }


	 public static <T extends Enum<T>> EnumSet<T> withEnumRemoved(EnumSet<T> set, T  off)
	 {
		 if( off == null || set.isEmpty() || !set.contains(off))
			 return set;

		 set = set.clone();
		 set.remove(off);
		 return set ;
	 }

	 public static <T extends Enum<T>> EnumSet<T> withEnumsRemoved(EnumSet<T> set,T...  off)
	 {
		 if( set.isEmpty()  || off.length == 0 )
			 return set ;
		 set = set.clone();
		 for( T e : off )
		    if( e != null)
			 set.remove(e);
		 return set ;
	 }


	 public static <T extends Enum<T>> EnumSet<T> withEnumsRemoved(EnumSet<T> set, EnumSet<T>  off)
	 {
		 if(  set.isEmpty()  || off.isEmpty() ||  ! setContainsAny( set , off) )
			 return set;
		 set = set.clone();
		 set.removeAll(off);
		 return set ;
	 }

	 // Return a new enum set with only those flags set in mask
	 // same as interesection
	 public static <T extends Enum<T>> EnumSet<T> withEnumsMasked(EnumSet<T> set, EnumSet<T>  mask)
	 {
		 if( set.isEmpty())
			 return set ;
		 set = set.clone();
		 set.retainAll(mask);
		 return set ;
	 }
	 public static <T extends Enum<T>> EnumSet<T> withEnumsMasked(EnumSet<T> set, T first , T...mask)
	 {
		 return withEnumsMasked( set , EnumSet.of( first , mask ) );
	 }

	 public static void wrapIOException(Throwable e) throws IOException {

		 if( e instanceof IOException )
			 throw (IOException) e ;
		 throw new IOException( e );

	 }

	 public static void wrapIOException(String message , Throwable e) throws IOException {

		 throw new IOException( message , e );

	 }
	 public static void wrapCoreException(String message , Throwable e) throws CoreException {

		throw mLogger.throwing(new CoreException( message , e) );

	 }

	 public static void safeClose(XMLEventWriter closable ) {
		 try {
			 if( closable != null )
				 closable.close();
		 } catch( Exception e )
		 {
			 mLogger.info("Exception closing: " + closable.getClass().getName() ,e);

		 }

	 }

	 // Calculate a new wait time given a current wait time
	 // if waitTime = 0 then means forever (return 0)
	 // if waitTime < 0 means no wait
	 public static long nextWait(long end, long waitTime)
	 {

		 if( waitTime <= 0 )
			 return waitTime ;

		 long now = System.currentTimeMillis() ;
		 if( now >= end )
			 return -1;
		 return end - now ;
	 }


	 public static String joinValues(List<XValue> args, String sep)
	 {
	   return join(args,sep);
	 }


	 // Iterator from start to ... end (exclusive)
	 public static Iterator<String> rangeIterator(final int start , final int end )
	 {
		 return new Iterator<String>() {
			 int pos = start ;


			 @Override
			 public boolean hasNext()
			 {
				 return start < end ;
			 }

			 @Override
			 public String next()
			 {
				 return String.valueOf( pos++ );
			 }

			 @Override
			 public void remove()
			 {
				 throw new UnsupportedOperationException();

			 }
		 };

	 }


	 public static Iterator<String> stringIterator(final Iterator<Integer> iterator)
	 {
		 return new Iterator<String>() {
			 Iterator<Integer> iter = iterator;

			 @Override
			 public boolean hasNext()
			 {
				 return iter.hasNext();
			 }

			 @Override
			 public String next()
			 {
				 return iter.next().toString();
			 }

			 @Override
			 public void remove()
			 {
				 iter.remove();

			 }
		 };
	 }

	 public static String join(Collection<?> args){
		 return join(args,",");
	 }

  public static String join(Collection<?> args, String sep)
  {
	  return join( new StringBuilder() , args , sep ).toString();
  }
  public static StringBuilder join(StringBuilder sb , Collection<?> args){
	  return join( sb , args , ",");
  }

  public static StringBuilder join(StringBuilder sb , Collection<?> args, String sep)
  {
      for( Object arg : args ){
        if( sb.length() > 0 )
          sb.append(sep);
        sb.append( arg.toString() );
      }
      return sb;
  }

  public static <T> List<T> toList(Iterator<T> iter)
  {
    List<T> list = new ArrayList<T>(  );
    while( iter.hasNext() )
      list.add(iter.next());
    return list ;
  }


  public static <E> Iterator<E> singletonIterator(final E e) {
      return new Iterator<E>() {
          private boolean hasNext = true;
          @Override
		public boolean hasNext() {
              return hasNext;
          }
          @Override
		public E next() {
              if (hasNext) {
                  hasNext = false;
                  return e;
              }
              throw new NoSuchElementException();
          }
          @Override
		public void remove() {
              throw new UnsupportedOperationException();
          }
      };
  }

  
  public static <E> Iterable<E> toIterable(final Iterator<E> iter)
  {
    return new Iterable<E>()
      {
        @Override
        public Iterator<E> iterator()
        {
          return iter;
        }
      };

  }

  public static <S, D> Iterator<D> toConvertingIterater(  
    Iterator<S> iter , 
    ITypeConverter<S, D> converter ){
  
     return new TypeConvertingIterator<S,D>( iter , converter );
    
  }

  // An iterator that converts from <S>ource type to <D> type
  public static <S, D> Iterable<D>
      toConvertingIterable(
        final Iterator<S> iterator, 
        final ITypeConverter<S, D> converter )
  {
    return new Iterable<D>()
        {
          @Override
          public Iterator<D> iterator()
          {
            return toConvertingIterater( iterator , converter );
          }
        }; 
  }


  public static boolean isOneOf(String s, String... strings)
  {

    for( String of: strings )
      if( Util.isEqual( s , of ) )
       return true;
    return false;
  
  }


public static Reader toReader(String string) {
	return new StringReader(string);
}


public static Reader toReader(URL scriptURL, String inputTextEncoding) throws IOException {
		return new InputStreamReader( scriptURL.openStream() , inputTextEncoding );
	
}

public static <T>  T[] toArray(T... v) {
    return v;
}


public static <T> boolean contains(T[] array, T v) {

	for( T a : array )
		if( a.equals(v) )
			return true;
	return false ;

}

	
	public static <T> ILogValue traceArray(final T[] array ) {
	   return new ILogValue() {
		   public String toString() {
			   return "[" + Util.stringJoin(array, ",") + "]" ;
		   } 
	   };
	}


	public static String stringConcat(String... values)
	{
		StringBuilder sb = new StringBuilder();
        for( String s : values)
        	sb.append(s);
        return sb.toString();
		
	}


	// Safe conversion in known encoding
	public static byte[] stringToAsciiBytes( String name ){
		return name.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		
	}

	public static byte[] stringToUTF8Bytes( String name ){
		return name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}


	public static <E extends Enum<E> > StringBuilder logString(StringBuilder sb , EnumSet<E> set) {
		return join( sb , set , ",");
		
	}


    public static <T> List<T> appendList(List<T> list1, List<T> list2 ) {
        if( list1 == null || list1.isEmpty() )
            return list2 ;
        if( list2 == null || list2.isEmpty() )
            return list1; 
        
        int n = list1.size() + list2.size();
        List<T> list = new ArrayList<T>( n );
        list.addAll( list1 );
        list.addAll(list2);
        return list ;
        
        
        
        
    }


    public static String stringJoin(String string, String sep, int length) {
        StringBuilder sb = new StringBuilder(string);
        while(--length > 0 )
            sb.append(sep).append(string);
        return sb.toString();
    }
    
    public static void require( boolean test , String message ) throws UnexpectedException
    {
        if( ! test )
            throw new UnexpectedException( message );
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
