/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StAXUtils;

public class xgetopts extends XCommand {

	private static final String kOPTION 	= "option";
	private static final String kOPTIONS 	= "options";
	private static final String kROOT 		= "xgetopts";
	private static final String kARG 		= "arg";
	private static final String kARGS 		= "args";
	private static final String kVALUE 	= "value";
	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = new Options("o=optdef:,c=command:,noargs,novalues",SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		String command = opts.getOptString("c", getShell().getArg0());
		String optdef = opts.getOptString("o", null);
		args = opts.getRemainingArgs();
		
		// Backwards compatible - arg[0] is optdef
		if( optdef == null ){
			if( args.size() == 0 ){
				usage();
				return 1;
			}
		
			optdef = args.remove(0).toString();
		}
		
		boolean bNoArgs = opts.hasOpt("noargs");
		boolean bNoValues = opts.hasOpt("novalues");
		
		
		XMLStreamWriter out = this.getStdout().asXMLStreamWriter(getSerializeOpts(opts));
		
		Options copts = new Options(optdef);
		
		List<OptionValue>  options = copts.parse(args);

		
		out.writeStartDocument();
		out.writeStartElement(kROOT);
		out.writeStartElement(kOPTIONS);

		
		for( OptionValue option : options ){
			out.writeStartElement(kOPTION);
			out.writeAttribute("name",option.getOptionDef().name);
			
			if( option.getOptionDef().hasArgs  ){
				
				
				
				for( XValue value : option.getValues() ) {
					
					int index = args.indexOf(value);
					out.writeStartElement( kVALUE );
					
					
					
					out.writeAttribute("index", String.valueOf(index) );
					
					
					if( ! bNoValues ){
						if( value.isAtomic())
							out.writeCharacters(value.toString());
						else
							write( out , value.asXdmNode() );
						
					}
					out.writeEndElement();
					

				}
			}
			out.writeEndElement();
			
		}
		out.writeEndElement();
		
		
		if( ! bNoArgs ){
			out.writeStartElement( kARGS );
	
			
			for( XValue value : copts.getRemainingArgs() ){
				out.writeStartElement(kARG);
				int index = args.indexOf(value);
				out.writeAttribute("index", String.valueOf(index) );
				
				
				if( ! bNoValues ){
			
	
					if( value.isAtomic())
						out.writeCharacters( value.toString());
					else
						write( out , value.asXdmNode() );
				}
				
				out.writeEndElement();
			}
			out.writeEndElement();
		}
		
		
		out.writeEndDocument();
		out.close();
		
		
		return 0;
	}

	private void write(XMLStreamWriter out, XdmNode node) throws XMLStreamException {
		
		StAXUtils.copy( node.getUnderlyingNode() , out );
		// XMLStreamUtils.copy( node.asSource() , out );

		
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
