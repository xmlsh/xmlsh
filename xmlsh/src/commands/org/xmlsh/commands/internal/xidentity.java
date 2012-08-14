/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.XdmItem;
import org.xmlsh.core.IXdmItemInputStream;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xidentity extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		
		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		OutputPort stdout = getStdout();
		if( stdout == null ){
			stdin.release();
		
			throw new InvalidArgumentException("Cannot open input");
		}
		try {
			
			setSerializeOpts(opts);
			
			IXdmItemInputStream input = stdin.asXdmItemInputStream(getSerializeOpts());
	
			IXdmItemOutputStream output = stdout.asXdmItemOutputStream(getSerializeOpts());
			
			
			XdmItem item ;
			
			while( (item = input.read() ) != null )
		        output.write(item);
			
			stdout.release();
				
		} 
		finally {
			
			stdin.release();
			stdout.release();
		}
		return 0;
		
		
	}

}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
