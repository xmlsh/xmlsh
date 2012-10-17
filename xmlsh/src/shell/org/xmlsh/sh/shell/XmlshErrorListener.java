/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class XmlshErrorListener implements ErrorListener {
	private Logger mLogger = LogManager.getLogger(XmlshErrorListener.class);

	
	private String formatError( TransformerException e , boolean isFatal )
	{ 
		StringBuffer sb  = new StringBuffer();
		 String errcat = (isFatal ? "Fatal error" : "Error");
         sb.append(errcat + " reported by XML parser: " + e.getMessage() + '\n');
         sb.append("  URL:    " + e.getLocator().getSystemId() + '\n');
         sb.append("  Line:   " + e.getLocator().getLineNumber() + '\n');
         sb.append("  Column: " + e.getLocator().getColumnNumber() + '\n');
         return sb.toString();
	}
	
	 
	@Override
	public void warning(TransformerException e) throws TransformerException {
		mLogger.warn(formatError(e ,false));

	}

	@Override
	public void error(TransformerException e) throws TransformerException {
		mLogger.error(formatError(e ,false));

	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException {
		mLogger.error(formatError(e ,true));
	}

}



//
//
//Copyright (C) 2008-2012 David A. Lee.
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
