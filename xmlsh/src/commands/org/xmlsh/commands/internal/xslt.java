/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import java.io.PrintStream;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellURIResolver;
import org.xmlsh.xpath.ShellContext;



public class xslt extends XCommand {

	private  class myErrorListener implements ErrorListener 
	{

		@Override
		public void error(TransformerException exception) throws TransformerException {
			printErr("Error: " + exception.getLocalizedMessage() );
			
		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException {
			printErr("Fatal Error: " + exception.getLocalizedMessage() );
			
		}

		@Override
		public void warning(TransformerException exception) throws TransformerException {
			printErr("Warning : " + exception.getLocalizedMessage() );
			
			
		}
		
	}
	private  class myMessageListener implements MessageListener 
	{

		@Override
		public void message(XdmNode content, boolean terminate, SourceLocator locator) {
			printErr( content.getStringValue() );
			
			
		}
	
	}
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{

		Options opts = new Options( "f:,i:,n,v" , args );
		opts.parse();
		PrintStream ps;
		Processor  processor  = Shell.getProcessor();

		XsltCompiler compiler = processor.newXsltCompiler();
		compiler.setErrorListener( new myErrorListener() );
		Source	context = null;

		InputPort in = null;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");

			if( ov != null )
				in = getInput( ov.getValue());
			else
				in = getStdin();
			context = in.asSource(getSerializeOpts());

		}

		Source source = null;

		Shell saved_shell = ShellContext.set(getEnv().getShell());

		try {
			List<XValue> xvargs = opts.getRemainingArgs();

			OptionValue ov = opts.getOpt("f");
			if( ov != null ){
				source = getInput(ov.getValue()).asSource(getSerializeOpts());
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
			eval.setMessageListener( new myMessageListener() );
			
			if( context != null )
				eval.setSource(  context  );


			if( opts.hasOpt("v")){
				// Read pairs from args to set
				for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
					String name = xvargs.get(i*2).toString();
					XValue value = xvargs.get(i*2+1);

					eval.setParameter( new QName(name),  value.asXdmValue() );	
				}			
			}

			OutputPort stdout = getStdout();
			eval.setDestination(stdout.asDestination(getSerializeOpts()));

			eval.transform();
			stdout.writeSequenceTerminator();
		} finally {
			ShellContext.set(saved_shell);
			// Closes stdin and kills xmlsh
			//if( in != null )
			//	in.close();

		}

		return 0;


	}


}



//Copyright (C) 2008,2009 , David A. Lee.

//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 

//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.

//The Original Code is: all this file.

//The Initial Developer of the Original Code is David A. Lee

//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.

//Contributor(s): none.

