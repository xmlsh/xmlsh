/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;

import org.xml.sax.SAXException;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.shell.FunctionDefinitions;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;

public class declare extends BuiltinCommand {


	@Override
	public int run(  List<XValue> args ) throws Exception {
		Options opts = new Options("p:,r:,t:,x:,f",SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts(opts );

		args = opts.getRemainingArgs();
		if( opts.hasOpt("p")) {
			 printVar( opts.getOptStringRequired("p"));
			 return 0;
		}
		else
		if( opts.hasOpt("x")){
		  export( opts.getOptStringRequired("x"));
          return 0;
		}
		
		if( opts.hasOpt("f")){
			if( args.isEmpty())
				printAllFuncs();
			else
			  printFunc( args.get(0).toString() );  
			return 
					0;
		}
		if( args.size() < 1 ) {
			printAllVars();
			return 1;

		}

		XValue what = args.remove(0);
		if( what.toString().equals("namespace"))
			return declareNamespace( args );

		usage();

		return 2;

	}


	private void printAllVars() throws UnsupportedEncodingException, CoreException, IOException {
		
		try ( PrintWriter w = this.getStdout().asPrintWriter(getSerializeOpts()) ){
			
		  for( String var : getShell().getEnv().getVarNames() )
			w.println(var);
		}
		
	}


	private void printAllFuncs() throws UnsupportedEncodingException, CoreException, IOException {
		  FunctionDefinitions fd = getShell().getFunctionDelcs();
		  if( fd  == null )
			  return;
		try ( PrintWriter w = this.getStdout().asPrintWriter(getSerializeOpts()) ){
			
		
			  for( String f :  fd.keySet() )
				w.println( f );
		}
		
	}


	private void printFunc(String name) throws UnsupportedEncodingException, CoreException, IOException {
		try ( PrintWriter w = this.getStdout().asPrintWriter(getSerializeOpts()) ){

			IFunctionDecl fd = getShell().getFunctionDecl(name);
			if( fd == null )
				w.println("Function: " + name + " not defined");
			else {
			  w.print("function ");
			  w.print(name);
			  w.println("()");
			   w.println( fd.getBody().toString(false) );
			  
			}
		}
		
	}


	private int export(String varname)
  { 
	  XVariable xvar = mShell.getEnv().getVar(varname); 
	  if( xvar == null ){
	    xvar = XVariable.newInstance(varname);
	  }
	  xvar.setFlag( XVarFlag.EXPORT);
	  mShell.getEnv().setVar(xvar);
    return 0;
	  
	  
  }


  private int printVar(String name) throws XMLStreamException, SAXException, IOException, CoreException, SaxonApiException
	{
		XEnvironment env = mShell.getEnv();


		XVariable var = env.getVar(name);
		if( var == null ) {
			this.printErr("Unknown variable: " + name );
			return 1 ;
		}

		OutputPort stdout = env.getStdout();
		XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());

		try {
			writer.writeStartDocument();
			writer.writeStartElement( getName() );
			var.serialize(writer);
			writer.writeEndElement();
			writer.writeEndDocument();


		} finally {
			if( writer != null)
				writer.close();
			stdout.writeSequenceTerminator(getSerializeOpts());
		}

		return 0;


	}


	private int declareNamespace(List<XValue> args) {
		if( args.size() == 0 )
			return listNamespaces();

		for( XValue arg : args){
			declareNamespace( arg );


		}
		return 0;

	}

	private int listNamespaces() {
		NameValueMap<String> ns = mShell.getEnv().getNamespaces();
		if( ns == null )
			return 0;

		for( String name : ns.keySet() ){
			String uri = ns.get(name);
			mShell.printOut(name + "=" + uri );

		}
		return 0;

	}

	/*
	 * Declare a namespace
	 * 
	 */


	private void declareNamespace(XValue arg) {

		// ns="url"
		// ns=
		// "url"
		if( arg.isAtomic() ){



			mShell.getEnv().declareNamespace(arg.toString());


		} else
		  usage("Unexpected value: "+ arg.describe() );

	}



}
//
//
//Copyright (C) 2008-2014    David A. Lee.
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
