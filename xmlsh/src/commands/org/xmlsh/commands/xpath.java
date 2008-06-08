/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.commands;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;



public class xpath extends XCommand {

	@Override
	public int run( List<XValue> args , XEnvironment env)
	throws Exception 
	{
		
		Options opts = new Options( "f:,i:,n,V" , args );
		opts.parse();
		
		Processor  processor  = Shell.getProcessor();
		
		XPathCompiler compiler = processor.newXPathCompiler();
		XdmNode	context = null;
		
		
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");
			DocumentBuilder builder = processor.newDocumentBuilder();

			if( ov != null)
				context = builder.build( env.getShell().getFile(ov.getValue().toString()));
			else
				context = builder.build( new StreamSource( env.getStdin()));
		
		}
		

		List<XValue> xvargs = opts.getRemainingArgs();
		

		
		OptionValue ov = opts.getOpt("f");
		String xpath = null;
		if( ov != null )
			xpath = Util.readString( env.getShell().getFile(ov.getValue().toString()) ) ;
		else 
			xpath = xvargs.remove(0).toString();
		

		
		if( opts.hasOpt("V")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();

				compiler.declareVariable(new QName(name));			
				
			}
				
			
		}
		
		
		

		XPathExecutable expr = compiler.compile( xpath );
		
		XPathSelector eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		if( opts.hasOpt("V")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				
				
				eval.setVariable( new QName(name),  value.toXdmValue() );	
					
				
			}
				
			
		}
		
		
		
/*
		Serializer dest = new Serializer();
		dest.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, "yes");
		dest.setOutputStream(env.getStdout());
*/		
		
		Writer w = new OutputStreamWriter(env.getStdout());
		for( XdmItem item : eval ){
			
			w.write( item.toString() + "\n");
			
		}
		w.flush();

		
		return 0;
		

	}

	/**
	 * @param args
	 * @throws XMLException 
	 */
	public static void main(String[] args) throws Exception {
		xpath cmd = new xpath();
		
		cmd.run( args );
		
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
