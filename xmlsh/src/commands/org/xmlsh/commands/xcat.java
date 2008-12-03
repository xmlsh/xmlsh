/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;

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
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;

public class xcat extends XCommand {

	

	
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "w:" , args );
		opts.parse();
		
		// root node
		OptionValue ow = opts.getOpt("w");
		XValue wrapper = null;
		if( ow != null )
			wrapper = ow.getValue();
		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
		

		Processor  processor  = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		XdmNode	context = null;
		
		
		/*
		 *  Special case, single file is taken as the context
		 */
		if(  xvargs.size() == 1 ){
			DocumentBuilder builder = processor.newDocumentBuilder();
			context = builder.build( getFile(xvargs.remove(0).toString()));

		}


		boolean hasFiles = ( xvargs.size() > 0 );
		
		if( context == null && ! hasFiles ){
			context = getStdin().asXdmNode();
		}
		
		
		
		
		String query = "";
		
		int wrapMode = 
			(wrapper == null ? 0 : wrapper.isString() ? 1 : 2 );
		
		int mode = (hasFiles ? 3 : 0 ) + wrapMode ;
		
		switch(mode){
		case 0:
			// No files no wrapper
			query = "root()"; break;
		case 1:
			// No files name wrapper
			query = "declare variable $wrapper as xs:string external;\n" +
					"element { $wrapper } { root() }"; 
			break;
			
		case	2:
		    // No files Element wrapper
			query = "declare variable $wrapper as node() external;\n" +
				    "element { $wrapper/name() } { $wrapper/@* , root() }";
			break;
		case	3:
			// files no wrapper
			query = "declare variable $files as xs:string* external;\n" +
				"let $wrapper := doc($files[1]) return element {$wrapper/node()/name()} {$wrapper/node()/@* , for $f in $files return doc($f)/node()/* }" ;
			break;
		case	4:
			
			// files string wrapper 
			query = 
				"declare variable $files as xs:string* external;\n" +
				"declare variable $wrapper as xs:string external;\n" +
				"element { $wrapper } { for $f in $files return doc($f)/node()/* }";
			break;
		case	5:
			// files Element wrapper
			query = 
				"declare variable $files as xs:string* external;\n" +
				"declare variable $wrapper as node() external;\n" +
				"element { $wrapper/name() } { $wrapper/@* , for $f in $files return doc($f)/node()/* }";
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
				files.add( new XValue( getFile(a).toURI().toString()));
			}
			eval.setExternalVariable( new QName("files"), new XValue(files).asXdmValue());
		
		}	
		
		eval.run(getStdout().asDestination());
		
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
