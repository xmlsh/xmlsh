/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;

import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
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



public class xquery extends XCommand {

	@Override
	public int run( List<XValue> args , XEnvironment env)
	throws Exception 
	{
		
		Options opts = new Options( "f:,i:,n,q:,v" , args );
		opts.parse();
		
		Processor  processor  = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		XdmNode	context = null;
		
		
		boolean bReadStdin = false ;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");
			DocumentBuilder builder = processor.newDocumentBuilder();
			
			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXExpr() ){
				XdmItem item = ov.getValue().toXdmValue().itemAt(0);
				if( item instanceof XdmNode )
					context = (XdmNode) item ; // builder.build(((XdmNode)item).asSource());
				 // context = (XdmNode) ov.getValue().toXdmValue();
			}
			if( context == null )
			{
	
				if( ov != null && ! ov.getValue().toString().equals("-"))
					context = builder.build( env.getShell().getFile(ov.getValue()));
				else {
					bReadStdin = true ;
					context = builder.build( new StreamSource( env.getStdin()));
				}	
			}
		}
		
		String query = null;
		
		if( opts.hasOpt("q"))
			query = opts.getOpt("q").getValue().toString();
		
		
		XQueryExecutable expr = null;
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
		OptionValue ov = opts.getOpt("f");
		if( ov != null ){
			if( query != null )
				throwInvalidArg( env, "Cannot specifify both -q and -f");
			String fname = ov.getValue().toString();
			if( fname.equals("-")){
				if( bReadStdin )
					throwInvalidArg( env , "Cannot read both query and context from stdin");
			
				query = Util.readString(env.getStdin());
			}
			else
				query =  Util.readString( env.getShell().getFile(fname));
	

		}

		
		if( query == null ){
			if ( xvargs.size() < 1 )
				throwInvalidArg(env,"No query specified");
			query = xvargs.remove(0).toString(); // remove arg 0
		}
		
			

		expr = compiler.compile( query );
		
		
		XQueryEvaluator eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				
				eval.setExternalVariable( new QName(name),  value.toXdmValue() );	
					
				
			}
				
			
		}
			

		
		
		Serializer dest = new Serializer();
		dest.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, "yes");
		dest.setOutputStream(env.getStdout());
		
		eval.run(dest);

		
		return 0;
		

	}

	/**
	 * @param args
	 * @throws XMLException 
	 */
	public static void main(String[] args) throws Exception {
		XCommand cmd = new xquery();
		
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
