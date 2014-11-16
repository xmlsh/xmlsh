/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;


public class http extends XCommand {


	private static Logger mLogger = LogManager.getLogger( http.class);

	/*
	 * Moved to config file
	 *
	 *
	static {
		LogManager.getLogger("httpclient").setLevel(Level.WARN);
		LogManager.getLogger("http.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.http").setLevel(Level.WARN);
	}

  */


	@Override
	public int run( List<XValue> args )
			throws Exception 
			{

		Options opts = new Options( "retry:,get:,put:,post:,head:,options:,delete:,connectTimeout:,contentType:,readTimeout:,+useCaches,+followRedirects,user:,password:,H=add-header:+,disableTrust:,keystore:,keypass:,sslproto:,output-headers=ohead:" );
		opts.parse(args);

		setSerializeOpts(getSerializeOpts(opts));  



		HttpRequestBase method;

		String surl = null;

		if( opts.hasOpt("get") ){
			surl =  opts.getOptString("get", null);
			method = new HttpGet(surl);
		}
		else
			if(  opts.hasOpt("put") ){
				surl =  opts.getOptString("put", null);
				method = new HttpPut(surl);
				((HttpPut)method).setEntity( getInputEntity(opts));
			}
			else
				if( opts.hasOpt("post") ){
					surl =  opts.getOptString("post", null);
					method = new HttpPost(surl);
					((HttpPost)method).setEntity( getInputEntity(opts));

				}
				else
					if( opts.hasOpt("head") ){
						surl =  opts.getOptString("head", null);
						method = new HttpHead(surl);
					}
					else
						if( opts.hasOpt("options") ){
							surl =  opts.getOptString("options", null);
							method = new HttpOptions(surl);
						}
						else
							if( opts.hasOpt("delete") ){
								surl =  opts.getOptString("delete", null);
								method = new HttpDelete(surl);
							}
							else
								if( opts.hasOpt("trace") ){
									surl =  opts.getOptString("trace", null);
									method = new HttpTrace(surl);
								}
								else {
									surl = opts.getRemainingArgs().get(0).toString();
									method = new HttpGet(surl);
								}


		if( surl == null ){
			usage();
			return 1;
		}


		int ret = 0;



		HttpHost host = new HttpHost(surl);

		DefaultHttpClient client = new DefaultHttpClient();




		setOptions( client , host ,  opts );

		OptionValue headers = opts.getOpt("H");
		if( headers != null ){

			for( XValue v : headers.getValues() ){
				StringPair pair = new StringPair( v.toString() , '=');
				method.addHeader(pair.getLeft(), pair.getRight());
			}

		}

		int retry = opts.getOptInt("retry", 0);
		long delay = 1000 ;


		HttpResponse resp = null ;

		do {
			try {
				resp =  client.execute(method);
				break ;
			} catch( IOException e ){
				mShell.printErr( "Exception running http" + ((retry > 0 ) ? " retrying ... " : "") , e);
				if( retry > 0 ){
					Thread.sleep( delay );
					delay *= 2 ;
				} else
					throw e ;
			}
		} while( retry-- > 0);


		HttpEntity respEntity = resp.getEntity();
		if( respEntity != null ){
			InputStream ins = respEntity.getContent();
			if( ins != null ){
				try {
					Util.copyStream(ins, getStdout().asOutputStream(getSerializeOpts()));
				} finally { 
					ins.close();
				}
			}
		}

		ret = resp.getStatusLine().getStatusCode() ;
		if( opts.hasOpt("output-headers"))
			writeHeaders( opts.getOptStringRequired("output-headers") , resp.getStatusLine(), resp.getAllHeaders());



		return ret;
			}






	private void writeHeaders(String outv, StatusLine statusLine, Header[] allHeaders) throws XMLStreamException, SaxonApiException, CoreException {
		OutputPort out = mShell.getEnv().getOutputPort(outv);

		XMLStreamWriter sw = out.asXMLStreamWriter(getSerializeOpts());
		sw.writeStartDocument();
		sw.writeStartElement("status");
		sw.writeAttribute("status-code", String.valueOf(statusLine.getStatusCode()));
		sw.writeAttribute("reason", statusLine.getReasonPhrase());
		sw.writeAttribute("protocol-version", statusLine.getProtocolVersion().toString());

		sw.writeEndElement();

		sw.writeStartElement("headers");
		for( Header header : allHeaders ){
			sw.writeStartElement("header");
			sw.writeAttribute("name", header.getName());
			sw.writeAttribute("value", header.getValue());
			sw.writeEndElement();
		}
		sw.writeEndElement();
		sw.writeEndDocument();
		sw.close();



	}






	private void setOptions(DefaultHttpClient client, HttpHost host, Options opts) throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, FileNotFoundException, KeyStoreException, IOException {

		HttpParams params = client.getParams();
		HttpConnectionParamBean connection = new HttpConnectionParamBean(params);

		if( opts.hasOpt("connectTimeout"))
			connection.setConnectionTimeout((int)(opts.getOptDouble("connectTimeout", 0) * 1000.));

		if( opts.hasOpt("readTimeout"))
			connection.setSoTimeout((int) (opts.getOptDouble("readTimeout", 0) * 1000.));


		/*
		if( opts.hasOpt("useCaches"))
			client.setUseCaches( opts.getOpt("useCaches").getFlag());


		if( opts.hasOpt("followRedirects"))

			client.setInstanceFollowRedirects(  opts.getOpt("followRedirects").getFlag());	






		String disableTrustProto = opts.getOptString("disableTrust", null);

		String	keyStore = opts.getOptString("keystore", null);
		String 	keyPass  = opts.getOptString("keypass", null);
		String  sslProto = opts.getOptString("sslProto", "SSLv3");

		if(disableTrustProto != null &&  client instanceof HttpsURLConnection )
			disableTrust( (HttpsURLConnection) client , disableTrustProto );

		else
		if( keyStore != null )
			setClient(  (HttpsURLConnection) client , keyStore , keyPass , sslProto );

		 */

		String disableTrustProto = opts.getOptString("disableTrust", null);

		if( disableTrustProto != null )
			disableTrust( client, disableTrustProto);


		String user = opts.getOptString("user", null);
		String pass = opts.getOptString("password", null);
		if( user != null && pass != null ){
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, pass);

			client.getCredentialsProvider().setCredentials( AuthScope.ANY , creds);
			/*
			// Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate DIGEST scheme object, initialize it and add it to the local
            // auth cache
            DigestScheme digestAuth = new DigestScheme();
            // Suppose we already know the realm name
            digestAuth.overrideParamter("realm", "some realm");
            // Suppose we already know the expected nonce value
            digestAuth.overrideParamter("nonce", "whatever");
            authCache.put(host, digestAuth);

            // Add AuthCache to the execution context
            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
			 */

		}



	}
	HttpEntity getInputEntity(Options opts) throws IOException, CoreException
	{

		AbstractHttpEntity entity = null ;
		InputPort in = getStdin();
		if( in.isFile() )
			entity =new FileEntity( in.getFile()  );

		else {
			byte[] data = Util.readBytes( in.asInputStream(getSerializeOpts()));
			entity = new ByteArrayEntity( data );
		}
		// return new InputStreamEntity( in.asInputStream(mSerializeOpts),-1);
		if( opts.hasOpt("contentType"))
			entity.setContentType(opts.getOptString("contentType", "text/xml"));

		return entity ;

	}

	private void disableTrust(DefaultHttpClient client , String disableTrustProto) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

		SSLSocketFactory socketFactory = new SSLSocketFactory(new TrustStrategy() {

			@Override
			public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
				return false;
			}

		}, org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		int port = client.getConnectionManager().getSchemeRegistry().getScheme(disableTrustProto).getDefaultPort();

		client.getConnectionManager().getSchemeRegistry().register(new Scheme(disableTrustProto, port, socketFactory));
	}





}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
