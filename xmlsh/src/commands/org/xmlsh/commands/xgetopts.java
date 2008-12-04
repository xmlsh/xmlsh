/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.util.XMLFactory;
import org.xmlsh.util.XMLSerializer;

public class xgetopts extends XCommand {

	private static final String kOPTION 	= "option";
	private static final String kOPTIONS 	= "options";
	private static final String kROOT 		= "xgetopts";
	private static final String kARG 		= "arg";
	private static final String kARGS 		= "args";
	private static final String kVALUE 	= "value";
	@Override
	public int run(List<XValue> args) throws Exception {
		if( args.isEmpty() ){
			usage();
			return 1;
		}
		
		XMLFactory fact = new XMLFactory();
		Document doc = fact.newDocument();
		
			
		String def =  args.remove(0).toString();
		Options opts = new Options(def,args);
		
		List<OptionValue>  options = opts.parse();
		args = opts.getRemainingArgs();
		
		Element root = doc.createElement(kROOT);
		doc.appendChild(root);
		
		Element eoptions = doc.createElement(kOPTIONS);
		root.appendChild(eoptions);

		
		for( OptionValue option : options ){
			Element eoption = doc.createElement(kOPTION);
			eoption.setAttribute("name",option.getOptionDef().name);
			if( option.getOptionDef().hasArgs  ){
				for( XValue value : option.getValues() ) {
					Element evalue = doc.createElement( kVALUE );
					
					
					if( value.isAtomic())
						evalue.setTextContent( value.toString());
					else
						evalue.appendChild( doc.importNode(value.asNodeWithDoc(),true) );

					eoption.appendChild(evalue);
				}
			}
			eoptions.appendChild(eoption);
			
		}
		
		Element eargs = doc.createElement(kARGS);
		root.appendChild(eargs);
		
		for( XValue value : opts.getRemainingArgs() ){
			Element eo = doc.createElement(kARG );
			

			if( value.isAtomic())
				eo.setTextContent( value.toString());
			else
				eo.appendChild( doc.importNode(value.asNodeWithDoc(),true) );
			
			eargs.appendChild(eo);
		}
		
		new XMLSerializer().write(doc, getStdout().asOutputStream());
		
		
		
		return 0;
	}

	private void usage() {
		this.getEnv().printErr("usage: xargs \"option def\" $*");
		
	}

}



//
//
//Copyright (C) 2008, David A. Lee.
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
