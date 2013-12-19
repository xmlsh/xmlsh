/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.stax;

import java.util.List;

import javax.xml.stream.XMLEventReader;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class closeReader extends XCommand {

	public closeReader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int run(List<XValue> args) throws Exception {
		if( args.size() != 1)
			throw new InvalidArgumentException("Expected XMLEventReader");
		
		Object obj = args.get(0).asObject();
		if(! (obj instanceof XMLEventReader))
			throw new InvalidArgumentException("Expected XMLEventReader");
		
		((XMLEventReader)obj).close();
		
		return 0;
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
