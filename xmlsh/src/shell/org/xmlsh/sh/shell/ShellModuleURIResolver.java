/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.trans.XPathException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.Util;

public class ShellModuleURIResolver implements ModuleURIResolver {
	private		Shell	mShell = null;
	private Logger mLogger = LogManager.getLogger();




	public ShellModuleURIResolver( Shell shell )
	{
		mShell = shell ;
	}


	@Override
	public StreamSource[] resolve(String moduleURI, String baseURI, String[] locations)
			throws XPathException {

		mLogger.debug("Resolve URI: " + moduleURI +  " " + baseURI + " " + Util.stringJoin( Arrays.asList(locations) , "|") );

		if( moduleURI.equals("http://www.functx.com")){

			URL url = mShell.getResource("/org/xmlsh/resources/modules/functx.xquery");
			if( url == null )
				return null ;

			try {
				URI uri = url.toURI();


				StreamSource[] result = new StreamSource[1];
				result[0] = new StreamSource( url.openStream() , uri.toString() );
				return result ;


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		return null;
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
