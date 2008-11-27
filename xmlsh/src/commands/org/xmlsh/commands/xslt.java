/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellURIResolver;



public class xslt extends XCommand {

	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		
		Options opts = new Options( "f:,i:,n,v" , args );
		opts.parse();
		
		Processor  processor  = Shell.getProcessor();
		
		XsltCompiler compiler = processor.newXsltCompiler();
		Source	context = null;
		
		

		boolean bReadStdin = false ;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");
			
			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXExpr() ){
				XdmItem item = ov.getValue().toXdmValue().itemAt(0);
				if( item instanceof XdmNode )
					context = ((XdmNode) item).asSource() ; // builder.build(((XdmNode)item).asSource());
				 // context = (XdmNode) ov.getValue().toXdmValue();
			}
			if( context == null )
			{
	
				if( ov != null && ! ov.getValue().toString().equals("-"))
					context = new StreamSource( getFile(ov.getValue()));
				else {
					bReadStdin = true ;
					context =  new StreamSource( getStdin());
				}	
			}
		}
		
		Source source = null;
		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
		OptionValue ov = opts.getOpt("f");
		if( ov != null ){
			String fname = ov.getValue().toString();
			if( fname.equals("-")){
				if( bReadStdin )
					throwInvalidArg( "Cannot read both xslt and context from stdin");
			
				source = new StreamSource(getStdin());
			}
			else
				source =  new StreamSource( new FileInputStream( getFile(fname)));
	

		}

		
		if( source == null ){
			throwInvalidArg("No xslt source specified");
		}
		
		/*
		 * Add namespaces -- DOESNT WORK FOR XSLT

		{
			NameValueMap<String> ns = getEnv().getShell().getNamespaces();
			if( ns != null ){
				for( String prefix : ns.keySet() ){
					String uri = ns.get(prefix);
					compiler.declareNamespace(prefix, uri);
					
				}
				
			}
		}
	  */
		

		XsltExecutable expr = compiler.compile( source );
		
		compiler.setURIResolver( new ShellURIResolver( compiler.getURIResolver()));
		XsltTransformer eval = expr.load();
		if( context != null )
			eval.setSource(  context  );
		
		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				
				eval.setParameter( new QName(name),  value.toXdmValue() );	
			}			
		}
			
		Serializer dest = new Serializer();
		dest.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, "yes");
		dest.setOutputStream(getStdout());
		eval.setDestination(dest);
		
		eval.transform();

		
		return 0;
		

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
