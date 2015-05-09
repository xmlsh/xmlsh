/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin.commands;

import static org.xmlsh.core.XVariable.XVarFlag.EXPORT;
import static org.xmlsh.core.XVariable.XVarFlag.LOCAL;
import static org.xmlsh.core.XVariable.XVarFlag.READONLY;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;

import org.xml.sax.SAXException;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.FunctionDefinitions;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class declare extends BuiltinCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = new Options("p,r,t,x,f,l,m,v,",
				SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts(opts);
		args = opts.getRemainingArgs();
		boolean bFound = false ;
		boolean hasP = opts.hasOpt("p");
		boolean hasAny = 
				 opts.hasOpt("f") ||
				 opts.hasOpt("v") ||
				 opts.hasOpt("m") ||
				 opts.hasOpt("n") ;
		
		boolean hasType = 
				opts.hasOpt("x") ||
				opts.hasOpt("l") ||
				opts.hasOpt("f") ||
				opts.hasOpt("r") ;
		

		boolean hasArgs = ! args.isEmpty();
		boolean hasNS  = hasArgs && args.get(0).toString().equals("namespace");

		if( ! hasP && ! hasArgs && ! hasNS  )
			hasP = true ;
            
			
		if( ! hasType && ! hasNS )
			hasP = true ;

		if( hasP && (!hasAny || opts.hasOpt("f")))
			bFound |= printFuncs(opts, args);
		if (hasP &&  (!hasAny || opts.hasOpt("v"))) 
			bFound |=  printVars(opts, args );
		if (hasP &&  (!hasAny || opts.hasOpt("m"))) 
			bFound |=  printModules(opts, args);
		if (hasP &&  (!hasAny || opts.hasOpt("n")))
			bFound |=  printNamespaces(opts, args);
		
		
		
		
		if( ! hasP && hasArgs ) {

		
			if( hasType )
				return declareType( opts , args );
		    else
		    if( hasNS ){
				XValue what = args.remove(0);
				if (what.toString().equals("namespace")) {
					if( args.isEmpty() )
						printNamespaces(opts, args);
					else
						declareNamespace(args.get(0));
				}
				else 
					usage();
				return -1;
			}
		} 
		else 
		if( ! bFound && !  args.isEmpty() ){
		    mShell.printErr( args.get(0) + ": not found");
			return 1;
		}
			
		return 0;
		


	}

	private int declareType(Options opts, List<XValue> args) throws InvalidArgumentException {

		
		if( opts.hasOpt("f") ){
			mShell.printErr("predeclaring functions is not implemented.");
			return 1 ;
		}
		
		EnumSet<XVarFlag> flags = XVariable.standardFlags();
		
		if( opts.hasOpt("x") )
			flags = Util.withEnumAdded(flags, EXPORT);
		if( opts.hasOpt("l") )
			flags = Util.withEnumAdded(flags, LOCAL);
		if( opts.hasOpt("r") )
			flags = Util.withEnumAdded(flags, READONLY);
		
		for( XValue a : args )
			mShell.getEnv().declareVar(a.toString(), flags);
		return 0 ;
		
		
	}

	private boolean  printNamespaces(Options opts, List<XValue> args) {
		boolean bFound = false ;
		if (args.isEmpty())
			bFound = listNamespaces();
		else {
			for (XValue a : args)
				bFound |= listNamespace(a.toString());
		}

		return bFound ;

	}

	private boolean printVars(Options opts, List<XValue> args)
			throws UnsupportedEncodingException, CoreException, IOException,
			XMLStreamException, SAXException, SaxonApiException {
		if (args.size() < 1) {
			return printAllVars();

		} else
			return printVar(args.get(0).toString());
	}

	private boolean printFuncs(Options opts, List<XValue> args)
			throws UnsupportedEncodingException, CoreException, IOException {
		if (args.isEmpty())
			return printAllFuncs();
		else
			return printFunc(args.get(0).toString());
		
	}

	private boolean printAllVars() throws UnsupportedEncodingException,
			CoreException, IOException {

		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			for (String var : getShell().getEnv().getVarNames())
				w.println(var);
		}
		return true ;

	}

	private boolean  printAllFuncs() throws UnsupportedEncodingException,
			CoreException, IOException {
		FunctionDefinitions fd = getShell().getFunctionDelcs();
		if (fd == null)
			return true ;
		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			for (Entry<String, IFunctionDefiniton> e : getShell().getEnv()
					.getFunctions().entrySet()) {

				w.printf("%s [%s]\n", e.getKey(), e.getValue().getModule()
						.getName());

			}
		}
		return true  ;

	}

	private boolean printFunc(String name) throws UnsupportedEncodingException,
			CoreException, IOException {
		boolean bFound = false;
		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			IFunctionDefiniton fd = getShell().getFunctionDecl(name);
			if (fd != null) {
				bFound = true ;

				w.print(name);
				w.println("()");
				w.println(fd.getBody().describe(false));

			}
		}
		return bFound ;

	}


	private boolean printVar(String name) throws XMLStreamException, SAXException,
			IOException, CoreException, SaxonApiException {
		XEnvironment env = mShell.getEnv();

		XVariable var = env.getVar(name);
		if (var == null) {
			return false ; 
		}

		OutputPort stdout = env.getStdout();
		XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());

		try {
			writer.writeStartDocument();
			writer.writeStartElement(getName());
			var.serialize(writer);
			writer.writeEndElement();
			writer.writeEndDocument();

		} finally {
			if (writer != null)
				writer.close();
			stdout.writeSequenceTerminator(getSerializeOpts());
		}

		return true ;

	}

	private int declareNamespace(List<XValue> args) {
		if (args.size() == 0)
			 listNamespaces();

		for (XValue arg : args) {
			declareNamespace(arg);

		}
		return 0;

	}

	private boolean listNamespace(String name) {
		String uri =  mShell.getEnv().getNamespaces().get(name);
		if (uri == null) 
			return false ;

		mShell.printOut(name + "=" + uri);

		return true ;
	}

	private boolean listNamespaces() {
		for (Entry<String, String> e :  mShell.getEnv().getNamespaces().entrySet() ) {
			mShell.printOut(e.getKey() + "=" + e.getValue() );
		}
		return true ;

	}

	/*
	 * Declare a namespace
	 */

	private int declareNamespace(XValue arg) {

		// ns="url"
		// ns=
		// "url"
		if (arg.isAtomic()) {

			mShell.getEnv().declareNamespace(arg.toString());

			return 0 ;

		} else
			usage("Unexpected value: " + arg.describe());
		return 2;
	}

	private boolean printModules(Options opts, List<XValue> args) {

		boolean bFound = false ;
		if (args.isEmpty()) {
			for (IModule hm : mShell.getEnv().getModules()) {
				printModule(hm);

			}
			return true ;
		} else {
			for (XValue a : args) {
				String name = a.toString();
				IModule hm = getEnv().getModules()
						.getExistingModuleByName(name);
				if( hm != null )
					bFound |= printModule(hm);
			}
		}
		return bFound ;

	}

	protected boolean  printModule(IModule hm) {
		assert( hm != null );
		
		XEnvironment env = getEnv();
		StringBuilder sb = new StringBuilder();
		String p = "";
		for (String prefix : env.getPrefixesForModule(hm)) {
			sb.append(p).append(prefix).append('=');
			p = " ";
		}

		String s = hm.describe();
		if (Util.isBlank(s))
			sb.append(hm.getName());
		else
			sb.append(s);

		mShell.printOut(sb.toString());
		return true ;
	}

}
//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
