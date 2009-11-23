/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NameValueMap;

public class xcat extends XCommand {
	/*
	 * TODO: xcat should use the collection() instead of doc()
	 * so that ports can be used as filenames instead of just files or URI's
	 */

	
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "w=wrap:,r=root,method:" , args );
		opts.parse();
		
		// root node
		OptionValue ow = opts.getOpt("w");
		XValue wrapper = null;
		if( ow != null )
			wrapper = ow.getValue();
		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
		

		Processor  processor  = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		NameValueMap<String> ns = getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}
			
		
		
		XdmNode	context = null;
		
		
		/*
		 *  Special case, single file is taken as the context
		 */
		if(  xvargs.size() == 1 ){
			DocumentBuilder builder = processor.newDocumentBuilder();
			context = builder.build( getSource(xvargs.remove(0)));

		}

		// hasFiles means 'has more then one file'
		// this effects if wrapping by default removes the root element.
		boolean hasFiles = ( xvargs.size() > 0 );
		boolean removeRoot = opts.hasOpt("r");
		
		
		
		
		// Use a copy of the serialize opts so we can override the method 
		SerializeOpts serializeOpts = getSerializeOpts().clone();
		if( opts.hasOpt("method"))
			serializeOpts.setMethod(opts.getOptString("method", "xml"));
			
		
		
		if( context == null && ! hasFiles ){
			context = getStdin().asXdmNode(serializeOpts);
		}
		
		
		
		
		String query = "";
		
		int wrapMode = 
			(wrapper == null ? 0 : wrapper.isString() ? 1 : 2 );
		
		int mode = (hasFiles ? 3 : 0 ) + wrapMode ;
		
		
		// Sub XQuery expression to return the sequence of nodes 
		// depending on if we remove the root
		
		// Query for where files are in a list
		String qfiles = 
			removeRoot ? 
			( wrapper == null ? 
					"for $f in $files return doc($f)/node()/*[1]" :
					"for $f in $files return doc($f)/node()/*" ) :
			"for $f in $files return doc($f)/node()" ;
		
		// Query for where files are in context
		String qcontext = 
			removeRoot ?
					(wrapper == null ? "./node()/*[1]" :  "./node()/*") :
					".";
					
		
		switch(mode){
		
		// stdin or 1 file - always maintains the root 
		case 0:
			// No files no wrapper
			query = qcontext ; break;
		case 1:
			// No files name wrapper
			query = "declare variable $wrapper as xs:string external;\n" +
					"element { $wrapper } { " + qcontext + " }"; 
			break;
			
		case	2:
		    // No files Element wrapper
			query = "declare variable $wrapper as node() external;\n" +
				    "element { $wrapper/name() } { $wrapper/@* , " + qcontext + " }";
			break;
		
		
		// File list specified (instead of context)
		// Optionally strips the root
		case	3:
			// files no wrapper
				query = "declare variable $files as xs:string* external;\n" +
					"let $wrapper := doc($files[1]) return element {$wrapper/node()/name()} {$wrapper/node()/@* , " + qfiles + " }" ;
				
			break;
		case	4:
			
			
			// files string wrapper 
			
			query = 
				"declare variable $files as xs:string* external;\n" +
				"declare variable $wrapper as xs:string external;\n" +
				"element { $wrapper } { " + qfiles + " }";
			break;
		case	5:
			// files Element wrapper
			query = 
				"declare variable $files as xs:string* external;\n" +
				"declare variable $wrapper as node() external;\n" +
				"element { $wrapper/name() } { $wrapper/@* , " + qfiles + " }";
			break;
		}
		
		

		
		XQueryExecutable expr = null;

		expr = compiler.compile( query );
		
		
		XQueryEvaluator eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		if( wrapper != null )
			eval.setExternalVariable( new QName("wrapper"),  wrapper.asXdmValue()  );	
		if( hasFiles ){
			ArrayList<XValue> files = new ArrayList<XValue>();
			for( XValue a : xvargs ){
				files.add( new XValue( (a).toString()));
			}
			eval.setExternalVariable( new QName("files"), new XValue(files).asXdmValue());
		
		}	
		
		OutputPort stdout = getStdout();
		eval.run(stdout.asDestination(serializeOpts));
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
