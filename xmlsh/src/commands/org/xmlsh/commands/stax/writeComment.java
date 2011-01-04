/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.stax;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class writeComment extends XCommand {

	
	
	public writeComment()
	{
		
	}
	
	@Override
	public int run( List<XValue> args) throws CoreException, XPathException, XMLStreamException {
		if( args.size()  != 2  )
			return -1;
		String comment = args.get(1).toString();
		XValue arg0 = args.get(0);
		Object arg = arg0.asObject();
		if( arg instanceof XMLStreamWriter )
			((XMLStreamWriter)arg).writeComment(comment);
		
		return 0;
		
	}

}



//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
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
