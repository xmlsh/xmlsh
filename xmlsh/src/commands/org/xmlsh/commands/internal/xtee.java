/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xtee extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(	SerializeOpts.getOptionDefs() , args );
		opts.parse();
		args = opts.getRemainingArgs();
		
		
		// List of outputs to tee to
		List<XMLEventWriter>	writers = new ArrayList<XMLEventWriter>();
		List<OutputPort>		closeme  = new ArrayList<OutputPort>();
		
		InputPort stdin = null;

		stdin = getStdin();
		
		
		
		
		try {
			
			SerializeOpts sopts = getSerializeOpts(opts);
			
			XMLEventReader	reader = stdin.asXMLEventReader(sopts);
			OutputPort stdout = getStdout();
			
			writers.add(stdout.asXMLEventWriter(sopts));
			
			for( XValue arg : args ){
				OutputPort output = getEnv().getOutput(arg, false);
				writers.add( output.asXMLEventWriter(sopts));
				closeme.add(output);
			}
		
			stdout.setSystemId(stdin.getSystemId());
			XMLEvent e;
			
			while( reader.hasNext() ){
				e = (XMLEvent) reader.next();
				for( XMLEventWriter writer : writers )
					writer.add(e);
			}
			
			reader.close();
			for( XMLEventWriter writer : writers )
				writer.close();
			
			// TODO: Why doesnt writers close the underlying stream ?
			// TODO: Do NOT Close stdout !
			for( OutputPort p : closeme )
				p.close();

		} 
		finally {
			
			stdin.close();
		}
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
