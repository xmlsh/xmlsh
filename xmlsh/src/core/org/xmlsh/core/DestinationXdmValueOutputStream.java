/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;

import org.xmlsh.util.Util;

class DestinationXdmValueOutputStream extends AbstractXdmItemOutputStream
{
	Destination 	mDest;


	DestinationXdmValueOutputStream( Destination dest ) 
	{
		mDest = dest ;

	}


	@Override
	public void write(XdmItem item) throws IOException {
		try {
			Util.writeXdmItem(item , mDest);
		} catch (SaxonApiException e) {
			throw new IOException("Exception writing XdmItem to output",e);
		}

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
