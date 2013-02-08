/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.get;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ContentbaseMetaData;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.SecurityOptions;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ItemType;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XSBoolean;
import com.marklogic.xcc.types.XSDate;
import com.marklogic.xcc.types.XSDecimal;
import com.marklogic.xcc.types.XSDouble;
import com.marklogic.xcc.types.XSDuration;
import com.marklogic.xcc.types.XSFloat;
import com.marklogic.xcc.types.XSGDay;
import com.marklogic.xcc.types.XSGMonth;
import com.marklogic.xcc.types.XSGYearMonth;
import com.marklogic.xcc.types.XSHexBinary;
import com.marklogic.xcc.types.XSInteger;
import com.marklogic.xcc.types.XSQName;
import com.marklogic.xcc.types.XSString;
import com.marklogic.xcc.types.XSUntypedAtomic;
import com.marklogic.xcc.types.XdmAttribute;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmVariable;

public abstract class MLCommand extends XCommand {

	protected	ContentSource	mContentSource = null ;
	protected 	Session 		mSession = null ;
	private    Logger mLogger = LogManager.getLogger(this.getClass());
	
	protected  PrintWriter		mOutput = null;

	protected boolean 		bVerbose = false ;
	
	public MLCommand() {
		super();
	}

	protected SecurityOptions newTrustOptions(URI uri) throws Exception
	{
		
		String scheme = uri.getScheme();
		if( !scheme.equals("xccs"))
			return null ;
		
		
		TrustManager[] trust = new TrustManager[] 
		  { 
				
				
			new X509TrustManager() {
				public void checkClientTrusted(
						X509Certificate[] x509Certificates, 
						String s)
				throws CertificateException 
					// nothing to do
					{
					
					}
					
					public void checkServerTrusted(
								X509Certificate[] x509Certificates, 
								String s)
						throws CertificateException {
							// nothing to do
					}
								
					public X509Certificate[] getAcceptedIssuers() {
									return null;
			
					}
			}
			};
			
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(null, trust, null);
			return new SecurityOptions(sslContext);

					
				
	}
			
	
	protected ContentSource getConnection(Options opts) throws Exception  {
		XValue vc = null;
		String connect;
		OptionValue ov = opts.getOpt("c");
		if (ov != null)
			vc = ov.getValue();
		else
			vc = getEnv().getVarValue("MLCONNECT");
		if (vc == null) {
			throw new InvalidArgumentException("No connection");
		}
		connect = vc.toString();

		URI serverUri = new URI(connect);
		ContentSource cs = ContentSourceFactory.newContentSource(serverUri,newTrustOptions(serverUri));
		return cs;
	}
	
	
	protected boolean writeResult(ResultSequence rs, OutputPort out, SerializeOpts sopts,
			boolean asText, boolean bBinary) throws InvalidArgumentException, IOException, CoreException, SaxonApiException 
	{
		OutputPort stdout = getStdout();
		
		IXdmItemOutputStream ser = null ;
		OutputStream os = null  ;
		PrintWriter  writer = null;
		
		if( bBinary )
			os = out.asOutputStream(sopts);
		else
		if( asText )
			writer = out.asPrintWriter(sopts);
		else
			ser = stdout.asXdmItemOutputStream(sopts);
		
		boolean bFirst = true ;
		boolean bAnyOut = false ;
		
		while (rs.hasNext()) {
			ResultItem rsItem = rs.next();

			XdmItem it = rsItem.getItem();
			
			
			bAnyOut = true ;
			if( ! bFirst )
				stdout.writeSequenceSeperator(sopts); // Thrashes variable output !
			bFirst = false ;
 
			if( bBinary )
				rsItem.writeTo(os );
			else
			if( asText )
				rsItem.writeTo( writer );
			else {
				
				net.sf.saxon.s9api.XdmItem item = null ;
				if (asText ) {
	 
					item = new  XdmAtomicValue( it.asString());
					
				}
				else
					item = asSaxonItem( it );
				
				ser.write(item);
			}
		}
	
		return bAnyOut;
	}

	protected boolean writeResult2(ResultSequence rs, OutputPort out, SerializeOpts sopts,
			boolean asText, boolean bBinary) throws FactoryConfigurationError, IOException,
			XMLStreamException, SaxonApiException, CoreException {

		XMLInputFactory factory = XMLInputFactory.newInstance();
		boolean bOutput = false ;
		while (rs.hasNext()) {
			ResultItem rsItem = rs.next();

			ItemType type = rsItem.getItemType();
			XdmItem it = rsItem.getItem();

			// NOTE: The following test doesnt work for attributes, known XCC
			// bug as of 2010-03-01

			if( bBinary ){
				OutputStream os = out.asOutputStream(sopts);
				rsItem.writeTo(os);
				os.close();
				
			}
			else
			
			if (asText || type.isAtomic() || (type.isNode() && it instanceof XdmAttribute)) {
				Writer os = out.asPrintWriter(sopts);

				rsItem.writeTo(os);
				os.close();
			} else {
				Reader isItem = rsItem.asReader();

				XMLEventWriter writer = out.asXMLEventWriter(sopts);

				XMLEventReader xmlItem = factory.createXMLEventReader(isItem);

				writer.add(xmlItem);
				writer.close();
				xmlItem.close();
			}

			if (!asText && rs.hasNext())
				out.writeSequenceSeperator(sopts);
			bOutput= true ;
		}
		return bOutput;
	}

	protected static String quote(String s) {

		return "'" + s.replace("'", "''").replace("&", "&amp;").replace("<", "&lt;") + "'";
	}

	protected XdmVariable newVariable(String name, XValue value, SerializeOpts opts)
			throws XPathException, InvalidArgumentException, SaxonApiException {
		XName xname = new XName(name);

		com.marklogic.xcc.types.XdmValue xvalue = newValue(value, opts);

		XdmVariable var = ValueFactory.newVariable(xname, xvalue);
		return var;
	}

	/*
	 * Create a marklogic XdmValue from an XValue
	 * 
	 * Truncates sequences to 1 element because ML doesnt support sequences
	 */
	protected com.marklogic.xcc.types.XdmValue newValue(XValue value, SerializeOpts opts)
			throws XPathException, InvalidArgumentException, SaxonApiException {

		if( value.isObject() ){
			String s = value.toString();
			return ValueFactory.newXSString(s);

			
			
			
		}
		
		
		net.sf.saxon.s9api.XdmItem item = value.asXdmItem();
		
		if (item.isAtomicValue()) {
			net.sf.saxon.value.AtomicValue atom = (net.sf.saxon.value.AtomicValue) item
					.getUnderlyingValue();
			// int type = atom.getItemType(null).getPrimitiveType();

			if (atom instanceof net.sf.saxon.value.Base64BinaryValue) {
				byte[] v = ((net.sf.saxon.value.Base64BinaryValue) atom).getBinaryValue();
				return ValueFactory.newBinaryNode(v);

			} else if (atom instanceof net.sf.saxon.value.BooleanValue) {
				boolean v = ((net.sf.saxon.value.BooleanValue) atom).effectiveBooleanValue();
				return ValueFactory.newXSBoolean(v);
			} else if (atom instanceof net.sf.saxon.value.DateTimeValue) {
				String v = ((net.sf.saxon.value.DateTimeValue) atom).toString();

				return ValueFactory.newXSDateTime(v, null, null);
			} else if (atom instanceof net.sf.saxon.value.GDateValue) {
				String v = ((net.sf.saxon.value.GDateValue) atom).toString();
				return ValueFactory.newXSDate(v, null, null);
			} else if (atom instanceof net.sf.saxon.value.TimeValue) {
				String v = ((net.sf.saxon.value.TimeValue) atom).toString();
				return ValueFactory.newXSTime(v, null, null);
			} else if (atom instanceof net.sf.saxon.value.DurationValue) {

				String v = ((net.sf.saxon.value.DurationValue) atom).toString();
				return ValueFactory.newXSDuration(v);
			} else if (atom instanceof net.sf.saxon.value.HexBinaryValue) {
				byte b[] = ((net.sf.saxon.value.HexBinaryValue) atom).getBinaryValue();
				return ValueFactory.newBinaryNode(b);

			} else if (atom instanceof net.sf.saxon.value.IntegerValue) {

				BigInteger v = ((net.sf.saxon.value.IntegerValue) atom).asBigInteger();

				return ValueFactory.newXSInteger(v);

			}
			else if (atom instanceof net.sf.saxon.value.DecimalValue) {
				BigDecimal v = ((net.sf.saxon.value.DecimalValue) atom).getDecimalValue();

				return ValueFactory.newValue(ValueType.XS_DECIMAL, v);

			}
			else if (atom instanceof net.sf.saxon.value.DoubleValue) {
				Double v = ((net.sf.saxon.value.DoubleValue) atom).getDoubleValue(); // box

				return ValueFactory.newValue(ValueType.XS_DOUBLE , v);

			}
			else if (atom instanceof net.sf.saxon.value.FloatValue) {
				Double v = ((net.sf.saxon.value.FloatValue) atom).getDoubleValue(); // box

				return ValueFactory.newValue(ValueType.XS_FLOAT , v);

			}

			else if (atom instanceof net.sf.saxon.value.NumericValue) {
				long v = ((net.sf.saxon.value.IntegerValue) atom).longValue();

				return ValueFactory.newXSInteger(v);

			} else {
				return ValueFactory.newXSString(atom.getStringValue());

			}

		}

		// Node node =
		// NodeOverNodeInfo.wrap(value.asXdmNode().getUnderlyingNode());
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		Util.writeXdmValue(value.asXdmNode(), Util.streamToDestination(buf, opts));

		InputStream is = new ByteArrayInputStream(buf.toByteArray());

		return ValueFactory.newElement(is);

	}

	/**
	 * Get the effective boolean value of the expression. This returns false if
	 * the value is the empty sequence, a zero-length string, a number equal to
	 * zero, or the boolean false. Otherwise it returns true.
	 * 
	 * @throws InvalidArgumentException
	 */

	protected boolean effectiveBoolean(ResultSequence rs) throws InvalidArgumentException {
		// Empty sequence
		if (rs.isEmpty())
			return false;

		if (rs.size() > 1)
			throw new InvalidArgumentException("Boolean value undefined for sequence of > 1 item");

		XdmItem item = rs.itemAt(0);
		ItemType type = item.getItemType();

		if (type.isAtomic()) {
			// Zero length string
			if (item instanceof XSString)
				return ((XSString) item).asString().length() == 0 ? false : true;

			// Numeber eq
			if (item instanceof XSInteger)
				return ((XSInteger) item).asPrimitiveInt() == 0 ? false : true;

			// Boolean
			if (item instanceof XSBoolean)
				return ((XSBoolean) item).asPrimitiveBoolean();

		}

		return true;
	}

	protected BigInteger[] parseForestIds(List<XValue> forests) throws RequestException {
		
		BigInteger bi[] = new BigInteger[ forests.size() ];
		
		ContentbaseMetaData meta = mSession.getContentbaseMetaData();
		int i = 0;
		for( XValue v : forests ){
			String forest = v.toString();
			BigInteger forest_id;
			if( Util.isInt(forest, false))
				forest_id = new BigInteger( forest );
			else
				forest_id = meta.getForestMap().get(forest);
			
			bi[i++] = forest_id ;
			
		}

		return bi;
		
		
		
		
		
	}

	
	

	private BigInteger[] toBigIntArray(List<XValue> values) {
		BigInteger bi[] = new BigInteger[ values.size() ];
		int i = 0;
		for( XValue v : values )
			bi[i++] = new BigInteger( v.toString() );
		return bi;
		
		
	}

	protected byte[] bytesFromItem( net.sf.saxon.s9api.XdmItem item, SerializeOpts opts ) throws SaxonApiException, UnsupportedEncodingException 
	{
        if( isAtomic(item) )
        	return item.toString().getBytes("UTF8");
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		Util.writeXdmItem(item, Util.streamToDestination(buf, opts)); // uses output xml encoding
		return		buf.toByteArray();
	}


	protected boolean isAtomic(net.sf.saxon.s9api.XdmItem item) {
		if( item == null )
			return true ;
		

		
		
		@SuppressWarnings("rawtypes")
		ValueRepresentation value = item.getUnderlyingValue();
		boolean isAtom = ( value instanceof AtomicValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isAtom;
	
		
	}
	// Dont use shell printError as these may be in non shell threads
		protected synchronized void printError( String error , Exception e )
		{		
			
			
			mOutput.println(error);
			mOutput.println(e.getMessage());
			for( Throwable t = e.getCause() ; t != null ; t = t.getCause() ){
				mOutput.println("  Caused By: " + t.getMessage());		
				
			}
			
			
			if( e != null )
				mLogger.error( error , e );
			
			mOutput.flush();
		}
	

		
		protected void print( String str )
		{
			if( bVerbose ){
				mOutput.println(str);
				mOutput.flush();
			}
				
		}
		
		protected net.sf.saxon.s9api.XdmItem asSaxonItem( XdmItem it ) throws SaxonApiException
		{
			ItemType type = it.getItemType();
			if( type.isAtomic() ){
				if( it instanceof XSBoolean )
					return new XdmAtomicValue(  ((XSBoolean)it).asPrimitiveBoolean() );
				else
				if( it instanceof XSDate )
					return new XdmAtomicValue( it.toString()  , net.sf.saxon.s9api.ItemType.DATE );
				else
				if( it instanceof XSDecimal )
					return new XdmAtomicValue(  ((XSDecimal)it).asBigDecimal() );
				else
				if( it instanceof XSDouble )
					return new XdmAtomicValue(  ((XSDouble)it).asPrimitiveDouble() );

				else
				if( it instanceof XSDuration )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.DURATION );
				else
				if( it instanceof XSFloat )
					return new XdmAtomicValue(   ((XSFloat)it).asPrimitiveFloat() );
				else
				if( it instanceof XSGDay )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.G_DAY );

				else
				if( it instanceof XSGMonth )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.G_MONTH );

				else
				if( it instanceof XSGYearMonth )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.G_YEAR_MONTH );
		
				else
				if( it instanceof XSHexBinary)
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.HEX_BINARY);
		

				else
				if( it instanceof XSInteger)
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.INTEGER );
				else
				if( it instanceof XSQName )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.QNAME );
				
				else
				if( it instanceof XSString )
					return new XdmAtomicValue(   ((XSString) it).toString() );
				else
				if( it instanceof XSUntypedAtomic )
					return new XdmAtomicValue(   it.toString()  , net.sf.saxon.s9api.ItemType.UNTYPED_ATOMIC );
		
				else
					return new XdmAtomicValue( it.toString() );

			}
			else
			if( type.isNode() ){
			 
				if( it instanceof XdmAttribute )
					return new XdmAtomicValue( it.toString() );
				else
				if( it instanceof XdmAttribute )
					return new XdmAtomicValue( it.toString() );
				else
			
				return 	Shell.getProcessor().newDocumentBuilder().build( new StreamSource( it.asReader()) );
					
				
				
				
			}
			else
				return new XdmAtomicValue( it.toString() );

			
			
			
		}

}

//
//
// Copyright (C) 2008-2012  David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
