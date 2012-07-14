/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.exist.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public abstract class ExistCommand extends XCommand {

	
	protected static final String sCOMMON_OPTS = "c=connect:,user:,password:,connectTimeout,readTimeout,useCaches,followRedirects,contentType:" ;
	
	
	public ExistCommand() {
		super();
	}
	
	protected	Options getOptions(  )	
	{

		return new Options( sCOMMON_OPTS , SerializeOpts.getOptionDefs());
		
	
			
	}
	
	protected	Options getOptions( String sopts )	
	{

		return new Options( sCOMMON_OPTS + "," + sopts , SerializeOpts.getOptionDefs());
		
	
			
	}

	protected ExistConnection getConnection(Options opts, String suri) throws MalformedURLException, IOException, URISyntaxException, InvalidArgumentException {
		
		String uri = getConnectionURI(opts, suri  );
		
		
		ExistConnection conn = new ExistConnection(mShell, uri , opts );
		return conn;
	}


	private String getConnectionURI(Options opts,String uri) throws URISyntaxException, InvalidArgumentException {
		
		String conn = opts.getOptString("c", mShell.getEnv().getVarString("EXIST_CONNECT"));
		if( Util.isBlank(conn))
			throw new InvalidArgumentException("No connection specified");
		
		if( ! conn.endsWith("/"))
			conn = conn + "/";
		
		if( ! Util.isBlank( uri ))
			conn = conn + uri ;
		
		
		
		return conn;
	}


	
	
	


/*
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
			
*/
	

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
