/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.builtin;

import java.net.URI;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.IXdmValueOutputStream;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xecho extends BuiltinCommand {
	
	public int run( List<XValue> args ) throws Exception {
		
		Options opts = new Options( "n,p=port:" , SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		args = opts.getRemainingArgs();
		boolean nolf = opts.hasOpt("n");
	
		String port = opts.getOptString("p", null);
		
		OutputPort stdout = 
			port != null ? mShell.getEnv().getOutputPort(port) : 
			mShell.getEnv().getStdout();

		if( stdout == null )
			throw new InvalidArgumentException("Output port not found: " + port );
		

		
		
		SerializeOpts serializeOpts = mShell.getSerializeOpts(opts);
		IXdmValueOutputStream dest =  stdout.asXdmItemOutputStream(serializeOpts);

		args = Util.expandSequences(args);
		
		boolean bFirst = true;
		for ( XValue arg : args ){
				if( ! bFirst )
					stdout.writeSequenceSeperator(serializeOpts);
				
				else {
					 XdmValue item = arg.asXdmValue();
					if( item instanceof XdmNode ){
						XdmNode xdmNode = ((XdmNode)item);
						// DAL: Note workaround bug in Saxon, crashes if getBaseURI on Attribute
						if( xdmNode.getNodeKind() != XdmNodeKind.ATTRIBUTE ){
							URI uri = xdmNode.getBaseURI();
							stdout.setSystemId( uri.toString() );
						}
					}
				}
					
				bFirst = false;
				dest.write( arg.asXdmValue() );
				
		}
		if( ! nolf )
			stdout.writeSequenceTerminator(serializeOpts);
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
