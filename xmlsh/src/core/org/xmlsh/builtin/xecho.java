/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.net.URI;
import java.util.List;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class xecho extends BuiltinCommand {
	
	public int run( List<XValue> args ) throws Exception {
		OutputPort stdout = mShell.getEnv().getStdout();
		Destination dest =  stdout.asDestination(mShell.getSerializeOpts());

		args = Util.expandSequences( args);
		
		boolean bFirst = true;
		for ( XValue arg : args ){
				if( ! bFirst )
					stdout.writeSequenceSeperator();
				
				else {
					 XdmValue item = arg.asXdmValue();
					if( item instanceof XdmNode ){
						URI uri = ((XdmNode)item).getBaseURI();
						stdout.setSystemId( uri.toString() );
					}
				}
				
				
				
				bFirst = false;
				Util.writeXdmValue(arg.asXdmValue(), dest);
		}

		stdout.writeSequenceTerminator();
		return 0;
	}
}
//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
