/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class AWSUtil {


	public static String getChecksum(AmazonS3 s3 , S3Path path ) {
		try {

			ObjectMetadata exists = s3.getObjectMetadata( path.getBucket() , path.getKey() );
			if( exists != null )
				return exists.getETag();
			
		} catch( Exception e ){
			
		}
		return null ;
	}

	/*
	 * Resolve a or x.x.x.x to IP address
	 */
	public static 	String	resolveDNS( String dns ) throws UnknownHostException
	{
		InetAddress addr = InetAddress.getByName(dns);
		if( addr == null )
			return null ;
		return addr.getHostAddress();
		
		
		
	}

	public static String parseBoolean(Boolean b) 
	{
		if( b == null )
			return "";
		else
			return b.toString();
		
		
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
