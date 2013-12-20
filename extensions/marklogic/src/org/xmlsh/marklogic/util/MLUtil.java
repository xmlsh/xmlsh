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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.SecurityOptions;
import com.marklogic.xcc.ValueFactory;
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
import com.marklogic.xcc.types.XdmVariable;

public class MLUtil {
	
	
	public static SecurityOptions newTrustOptions(URI uri) throws Exception {
		return newTrustOptions( uri.getScheme() );
	}

	public static SecurityOptions newTrustOptions(String scheme) throws Exception
	{
		
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

	public static String quote(String s) {
	
		return "'" + s.replace("'", "''").replace("&", "&amp;").replace("<", "&lt;") + "'";
	}

	public static  XdmVariable newVariable(String name, XValue value, SerializeOpts opts)
			throws XPathException, InvalidArgumentException, SaxonApiException {
		XName xname = new XName(name);
	
		com.marklogic.xcc.types.XdmValue xvalue = MLUtil.newValue(value, opts);
	
		XdmVariable var = ValueFactory.newVariable(xname, xvalue);
		return var;
	}
	
	
	public static  XdmVariable newVariable(String name, com.marklogic.xcc.types.XdmValue xvalue)
			throws XPathException, InvalidArgumentException, SaxonApiException {
		XName xname = new XName(name);
		XdmVariable var = ValueFactory.newVariable(xname, xvalue);
		return var;
	}

	public static  XdmVariable newVariable(String name, String value)
			 {
		XName xname = new XName(name);
		XdmVariable var = ValueFactory.newVariable(xname, ValueFactory.newXSString(value ) );
		return var;
	}
	/*
	 * Create a marklogic XdmValue from an XValue
	 * 
	 * Truncates sequences to 1 element because ML doesnt support sequences
	 */
	public  static com.marklogic.xcc.types.XdmValue newValue(XValue value, SerializeOpts opts)
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
	
	public static boolean effectiveBoolean(ResultSequence rs) throws InvalidArgumentException {
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

	public static  BigInteger[] toBigIntArray(List<XValue> values) {
		BigInteger bi[] = new BigInteger[ values.size() ];
		int i = 0;
		for( XValue v : values )
			bi[i++] = new BigInteger( v.toString() );
		return bi;
		
		
	}

	public static  boolean isAtomic(net.sf.saxon.s9api.XdmItem item) {
		if( item == null )
			return true ;
		
	
		
		
		@SuppressWarnings("rawtypes")
		ValueRepresentation value = item.getUnderlyingValue();
		boolean isAtom = ( value instanceof AtomicValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isAtom;
	
		
	}

	public static  byte[] bytesFromItem( net.sf.saxon.s9api.XdmItem item, SerializeOpts opts ) throws SaxonApiException, UnsupportedEncodingException 
	{
	    if( isAtomic(item) )
	    	return item.toString().getBytes("UTF8");
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
	
		Util.writeXdmItem(item, Util.streamToDestination(buf, opts)); // uses output xml encoding
		return		buf.toByteArray();
	}

	public static net.sf.saxon.s9api.XdmItem asSaxonItem( XdmItem it ) throws SaxonApiException
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

	public static boolean writeResult(ResultSequence rs, OutputPort out, SerializeOpts sopts,
			boolean asText, boolean bBinary) throws InvalidArgumentException, IOException, CoreException, SaxonApiException 
	{
		
		IXdmItemOutputStream ser = null ;
		OutputStream os = null  ;
		PrintWriter  writer = null;
		
		if( bBinary )
			os = out.asOutputStream(sopts);
		else
		if( asText )
			writer = out.asPrintWriter(sopts);
		else
			ser = out.asXdmItemOutputStream(sopts);
		
		boolean bFirst = true ;
		boolean bAnyOut = false ;
		
		while (rs.hasNext()) {
			ResultItem rsItem = rs.next();
	
			XdmItem it = rsItem.getItem();
			
			
			bAnyOut = true ;
			if( ! bFirst )
				out.writeSequenceSeperator(sopts); // Thrashes variable output !
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

	public static XdmVariable newVariable(String name, long value) {
		XName xname = new XName(name);
		XdmVariable var = ValueFactory.newVariable(xname, ValueFactory.newXSInteger(value) );
		return var;
	}

	public static int getIntValue(Object value) {
		if( value instanceof String )
			return Integer.parseInt((String) value);
		if( value instanceof Integer )
			return ((Integer)value).intValue();
		if( value instanceof Long ) 
			return ((Long)value).intValue() ;
		
		return Integer.parseInt(value.toString());
		
		
		
	}

}



/*
 * Copyright (C) 2008-2014 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */