/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.exist.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.StreamOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Base64Coder;
import org.xmlsh.util.Util;

public class ExistConnection {
	private static final String kEXIST_NS = "http://exist.sourceforge.net/NS/exist";

    private final static String kSER_NAMESPACE = "http://exist-db.org/xquery/types/serialized";
    private final static String kSEQ_ELEMENT = "sequence";
    private final static String kVALUE_ELEMENT = "value";
    

    private final static String kATTR_TYPE = "type";
    private final static String kATTR_ITEM_TYPE = "item-type";
    
    
    
	private		HttpURLConnection		mHttp ;
	private		Shell					mShell;
	private		Map<String,XValue>		mVariables = new HashMap<String,XValue>();
	private		Map<String,String>		mProperties = new HashMap<String,String>();
	private		SerializeOpts			mSerializeOpts ;
	private		String						mURI;
	private		Options					mOptions;
	
	protected	Namespaces mNs = null ;
	
	public ExistConnection(Shell shell , String uri ,  Options options ) throws MalformedURLException, IOException, InvalidArgumentException {
		mURI = uri ;
		mOptions = options;
		mSerializeOpts = shell.getSerializeOpts(options) ;
		
		parseSerializesOpts( options );
		
		
		mShell = shell ;

	}


	
	
	private void parseSerializesOpts(Options options) {
		if( options.hasOpt("text"))
			mSerializeOpts.setContent_type("text/plain");
		else
		if( options.hasOpt("binary"))
			mSerializeOpts.setContent_type("application/octet-stream");
		else
		if( options.hasOpt("xml"))
			mSerializeOpts.setContent_type("text/xml");
		else
		if( options.hasOpt("xquery"))
			mSerializeOpts.setContent_type("application/xquery");

		else
		if( options.hasOpt("contentType"))
			mSerializeOpts.setContent_type( options.getOptString("contentType", mSerializeOpts.getContent_type()));
		
	}


	public void resolveNamespaces( Options opts )
	{
		/*
		 * Add namespaces
		 */
		
		
		if( !opts.hasOpt("nons"))
			mNs = mShell.getEnv().getNamespaces();
		
		if( opts.hasOpt("ns")){
			Namespaces ns2 = new Namespaces();
			if( mNs != null )
				ns2.putAll(mNs);
			
			// Add custom name spaces
			for( XValue v : opts.getOpt("ns").getValues() )
				ns2.declare(v);
				
			
			mNs = ns2;
		}
		
	}
	
	public void setVariable(String name, XValue value) {
		mVariables.put(name,value);
		
	}
	public void setProperty( String name, String value)
	{
		mProperties.put(name, value);
	}

	public int query(String query,boolean bRaw, boolean bMeta, boolean bCache, int start, int max, String session, OutputPort out) throws XMLStreamException, SaxonApiException, IOException, CoreException, URISyntaxException {
		
		
		String suri = mURI ;
		// if( !mVariables.isEmpty())
		//	suri += "?" + encodeVariables();
		
		
		
	//	mShell.printErr(suri);
		URL url = new URL( suri );
		
		
		mHttp = (HttpURLConnection) url.openConnection();
		
		
		setOptions( mHttp , mOptions );
		resolveNamespaces( mOptions );
		
		
		String body = formatQuery(query,bCache,start,max,session);
		// mShell.printErr(body);
		
		
		mHttp.setRequestMethod("POST");
		

		mHttp.setDoInput(true);
		mHttp.setDoOutput(true);
		byte[] bodyBytes = body.getBytes(mSerializeOpts.getOutputXmlEncoding());	// Output encoding - returned from exist
		mHttp.setRequestProperty("Content-Length", Long.toString(bodyBytes.length));
		mHttp.setRequestProperty("Content-Type", "application/xml");

		
		
		
		mHttp.connect();
		int ret;
		try {
			

			
			
			OutputStream httpout = mHttp.getOutputStream();
			httpout.write( bodyBytes );
			httpout.flush();
			httpout.close();
				
			ret = mHttp.getResponseCode();
			if( ret == 200 )
				ret = 0;
			if( ret == 0 ){
				InputStream in = mHttp.getInputStream();
				writeResult(in, out, bRaw, bMeta);
				in.close();
			}
				
			
		} finally {
			mHttp.disconnect();
		}
		
		
		return ret ;
		
		
	}

	private String encodeVariables() throws UnsupportedEncodingException {
		
		StringBuffer encoded = new StringBuffer();
		
		for( Entry<String,XValue> entry :  mVariables.entrySet()){
			if( encoded.length() > 0)
				encoded.append('&');
			encoded.append(entry.getKey());
			encoded.append('=');
		
			encoded.append(URLEncoder.encode(entry.getValue().toString(),"UTF-8"));
		}
				
		
		return encoded.toString();
	}


	private void writeResult(InputStream ins, OutputPort outp, boolean bRaw, boolean bMeta) throws IOException, CoreException, SaxonApiException, URISyntaxException {
		
		if( bRaw ){
			OutputStream os = outp.asOutputStream(mSerializeOpts);
			Util.copyStream(ins, os);
			os.close();
			return ;
			
			
			
		}
		
		
		
		IXdmItemOutputStream outs = outp.asXdmItemOutputStream(mSerializeOpts);
		
		
		// InputPort inp = new StreamInputPort(ins, null);
		
		
		// Util.copyStream(ins, outp.asOutputStream(mSerializeOpts));
		
		
		
		XQueryExecutable exe = getXQuery("result.xquery");
		XQueryEvaluator eval = exe.load();
		eval.setExternalVariable( new QName("meta"), new XValue(bMeta).asXdmValue());
		
		eval.setSource( new StreamSource(ins));

		
		boolean bAnyOut = false ;
		boolean bFirst = true ;
		boolean bString = false ;
		
		for( XdmItem item : eval ){
			bAnyOut = true ;
			if( ! bFirst )
				outp.writeSequenceSeperator(mSerializeOpts); // Thrashes variable output !
			bFirst = false ;
			
			
			if( item instanceof XdmNode ){
				XdmNode node = (XdmNode) item ;
				if( bString  )
					item = new XdmAtomicValue( node.getStringValue());
				
			}
			
			
			//processor.writeXdmValue(item, ser );
			// Util.writeXdmValue(item, ser);
			outs.write(item);
			
		}
		if( bAnyOut )
			outp.writeSequenceTerminator(mSerializeOpts); // write "\n"

				
		
		
	}

	
	private XQueryExecutable getXQuery(String string) throws SaxonApiException, IOException, URISyntaxException {

		XQueryCompiler mXQueryCompiler = Shell.getProcessor().newXQueryCompiler();

		String resPath = "/org/xmlsh/exist/resources/" + string ;
		URL url = mShell.getResource(resPath);
		if( url == null )
			throw new URISyntaxException(resPath, "Cannot locate resource"  );
		
		
		InputStream isQuery = url.openStream();
		try {
			
			if( url != null ){
				URI uri = url.toURI();
				mXQueryCompiler.setBaseURI(  uri );
			}
			
			return mXQueryCompiler.compile( isQuery );
		} finally {
			isQuery.close();
		}
		
	}
	
	
	
	private String formatQuery(String query, boolean bCache, int start, int max, String session) throws InvalidArgumentException, XMLStreamException, SaxonApiException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		OutputPort	out = new StreamOutputPort(buf);
		
		
		// Need to modify the serialization opts to get past eXists very pecurlar parser
		// Some of the parsing code is whitespace sensitive
		
		SerializeOpts ser = mSerializeOpts.clone();
		ser.setIndent(false);
		ser.setOmit_xml_declaration(false);
		
		XMLStreamWriter w = out.asXMLStreamWriter(ser);
		
		w.writeStartDocument();
		w.setDefaultNamespace( kEXIST_NS);
		w.writeStartElement(kEXIST_NS , "query");
		w.writeAttribute("cache", bCache ? "yes" : "no");
		w.writeAttribute("start", String.valueOf(start));
		w.writeAttribute("max" , String.valueOf(max));
		if( !Util.isBlank(session))
			w.writeAttribute("session_id", session);
		
		writeProperties(w);
		// writeVariables(w);
		w.writeStartElement(kEXIST_NS , "text");
		w.writeCData(query);
		w.writeEndElement();
		w.writeEndElement();
		w.writeEndDocument();
		w.flush();
		w.close();
		
		return buf.toString();
		
		
		
	}

	private void writeVariables(XMLStreamWriter w) throws XMLStreamException {

		if(! mVariables.isEmpty()){
			w.writeStartElement( kEXIST_NS , "variables");
			for( Entry<String,XValue> entry :  mVariables.entrySet()){
				w.writeStartElement( kEXIST_NS , "variable");
				
					QName vqname = Util.resolveQName( entry.getKey() , mNs );
					w.writeStartElement(kEXIST_NS,"qname");
						w.writeStartElement(kEXIST_NS, "localname");
						w.writeCharacters(vqname.getLocalName());
						w.writeEndElement();
						
						if( !Util.isBlank(vqname.getNamespaceURI())){
							w.writeStartElement(kEXIST_NS, "namespace");
							w.writeCharacters(vqname.getNamespaceURI());
							w.writeEndElement();
						}
						if( !Util.isBlank(vqname.getPrefix())){
							w.writeStartElement(kEXIST_NS, "prefix");
							w.writeCharacters(vqname.getPrefix());
							w.writeEndElement();
						}
					w.writeEndElement();
					
					// Value
					w.writeStartElement( "sx" , kSEQ_ELEMENT , kSER_NAMESPACE );
					for( XdmItem item : entry.getValue().asXdmValue() ){
						w.writeStartElement( "sx", kVALUE_ELEMENT, kSER_NAMESPACE  );
						w.writeAttribute( kATTR_TYPE, "xs:string");
						w.writeCharacters(item.getStringValue());
						w.writeEndElement();
						
						
					}
					w.writeEndElement();
				w.writeEndElement();
			}
			w.writeEndElement();
			
		}

	}

	private void writeProperties(XMLStreamWriter w) throws XMLStreamException {
		if(! mProperties.isEmpty()){
			w.writeStartElement( kEXIST_NS , "properties");
			for( Entry<String,String> entry :  mProperties.entrySet()){
				w.writeStartElement( kEXIST_NS , "property");
				w.writeAttribute( "name" , entry.getKey() );
				w.writeAttribute( "value" , entry.getValue() );
				w.writeEndElement();
			}
			w.writeEndElement();
			
		}
		
	}

	public void close() {
		mHttp = null ;
	}



	private void setOptions(HttpURLConnection http,  Options opts) throws UnsupportedEncodingException {
		
		if( opts.hasOpt("connectTimeout"))
			http.setConnectTimeout( (int)(opts.getOptDouble("connectTimeout", 0) * 1000.) );
		
		if( opts.hasOpt("readTimeout"))
			http.setReadTimeout( (int) (opts.getOptDouble("readTimeout", 0) * 1000.));
		
		if( opts.hasOpt("useCaches"))
			http.setUseCaches( opts.getOpt("useCaches").getFlag());
		
		if( opts.hasOpt("followRedirects"))
			http.setInstanceFollowRedirects(  opts.getOpt("followRedirects").getFlag());	
		

		if( opts.hasOpt("contentType"))
			http.setRequestProperty("Content-Type", opts.getOptString("contentType", "application/xml"));

		
		String user = opts.getOptString("user", mShell.getEnv().getVarString("EXIST_USER"));
		String pass = opts.getOptString("password", mShell.getEnv().getVarString("EXIST_PASSWORD"));
		if( user != null && pass != null ){
			String up = user + ":" + pass ;
			
			 // Encode String
			
		       String encoding = new String(Base64Coder.encode (up.getBytes("US-ASCII")));
		       
		       http.setRequestProperty  ("Authorization", "Basic " + encoding);
			
			
			
		}
		
		
		
		
		
	}

	public int put(InputPort in) throws IOException, CoreException {
		
		
		String suri = mURI ;
		URL url = new URL( suri );
		
		
		mHttp = (HttpURLConnection) url.openConnection();
		
		
		mHttp.setRequestMethod("PUT");
		

		mHttp.setDoInput(true);
		mHttp.setDoOutput(true);
		
		
		mHttp.setRequestProperty("Content-Type",  mSerializeOpts.getContent_type());
		

		
		setOptions( mHttp , mOptions );

		
		mHttp.connect();
		int ret;
		try {
			

			
			
			OutputStream httpout = mHttp.getOutputStream();
			InputStream is = in.asInputStream(mSerializeOpts);
			Util.copyStream(is, httpout);
			
			is.close();
			
			httpout.flush();
			httpout.close();
				
			
			
			
			ret = mHttp.getResponseCode();
			if( ret == 200 )
				ret = 0;
			
				
			
		} finally {
			mHttp.disconnect();
		}
		
		
		return ret ;
		
		
		
		
	}




	public int get(OutputPort out) throws IOException {
	
		String suri =  mURI + "?" + getParams();
		
		URL url = new URL( suri );
		
		
		mHttp = (HttpURLConnection) url.openConnection();
		
		
		mHttp.setRequestMethod("GET");
		

		mHttp.setDoInput(true);
		mHttp.setDoOutput(false);
		
		
		mHttp.setRequestProperty("Content-Type",  mSerializeOpts.getContent_type());
		

		
		setOptions( mHttp , mOptions );

		
		mHttp.connect();
		int ret;
		try {
			
			InputStream is = mHttp.getInputStream();

			

			OutputStream os = out.asOutputStream(mSerializeOpts);
			Util.copyStream(is, os);
			
			is.close();
			os.flush();
			
			
			
			
			
			
			ret = mHttp.getResponseCode();
			if( ret == 200 )
				ret = 0;
			
				
			
		} finally {
			mHttp.disconnect();
		}
		
		
		return ret ;
		
		
		
		
		
		
		
	}





	private String getParams( ) {
		
		StringBuffer sb = new StringBuffer( );
		
		addParam( sb , "_xsl" , "no");
		addParam( sb , "_indent" ,  mSerializeOpts.isIndent() ? "yes" : "no");
		addParam( sb , "_encoding", mSerializeOpts.getOutputXmlEncoding() );
		addParam( sb , "_source" , "yes");
		
		return sb.toString();
		
		
		
	}

	private String getInvokeParams( ) {
		
		StringBuffer sb = new StringBuffer( );
		
		addParam( sb , "_xsl" , "no");
		addParam( sb , "_indent" ,  mSerializeOpts.isIndent() ? "yes" : "no");
		addParam( sb , "_encoding", mSerializeOpts.getOutputXmlEncoding() );
		addParam( sb , "_wrap", "yes");
		
		return sb.toString();
		
		
		
	}


	private void addParam( StringBuffer sb , String name, String value) 
	{
		if( sb.length() >  0)
			sb.append("&");
		sb.append(name);
		sb.append('=');
		sb.append( Util.urlEncode(value));
	}




	public int invoke(InputPort in, OutputPort out, boolean bRaw) throws IOException, CoreException, SaxonApiException, URISyntaxException {
		
		boolean bPost = (in != null );
		
		// if POST then dont add query parameters - doesnt work 
		String suri  =  bPost ? mURI  : (mURI + "?" + getInvokeParams());


		URL url = new URL( suri );
		
		
		mHttp = (HttpURLConnection) url.openConnection();
		
		
		setOptions( mHttp , mOptions );
		resolveNamespaces( mOptions );
		
		
		if( bPost ) {
			mHttp.setRequestMethod("POST");
			// POST implies RAW - eXists problem wont wrap results for POST
			bRaw = true ;
		}
		else 
			mHttp.setRequestMethod("GET");
		

		mHttp.setDoInput(true);
		mHttp.setDoOutput(bPost  );
		

		
		
		
		mHttp.connect();
		int ret;
		try {
			


			if( bPost  ){
				OutputStream httpout = mHttp.getOutputStream();
				InputStream is = in.asInputStream(mSerializeOpts);
				Util.copyStream(is, httpout);
				is.close();
				
				
				httpout.flush();
				httpout.close();
			}
				
			ret = mHttp.getResponseCode();
			if( ret == 200 )
				ret = 0;
			if( ret == 0 ){
				InputStream httpin = mHttp.getInputStream();
				writeResult(httpin, out, bRaw, false);
				httpin.close();
			}
				
			
		} finally {
			mHttp.disconnect();
		}
		
		
		return ret ;
		
		
		
		
		
		
		
		
		
	}




	public int del() throws IOException {
String suri =  mURI + "?" + getParams();
		
		URL url = new URL( suri );
		
		
		mHttp = (HttpURLConnection) url.openConnection();
		
		
		mHttp.setRequestMethod("DELETE");
		

		mHttp.setDoInput(true);
		mHttp.setDoOutput(false);
		
		
		setOptions( mHttp , mOptions );

		
		mHttp.connect();
		int ret;
		try {
			
	
			ret = mHttp.getResponseCode();
			if( ret == 200 )
				ret = 0;
			
				
			
		} finally {
			mHttp.disconnect();
		}
		
		
		return ret ;
		
		
	}

	
	

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
