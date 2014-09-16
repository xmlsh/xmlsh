/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map.Entry;

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
import org.xmlsh.sh.shell.ModuleHandle;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;

public class declare extends BuiltinCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = new Options("p,r:,t:,x:,f,m,v,",
				SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts(opts);
		args = opts.getRemainingArgs();
		boolean hasP = opts.hasOpt("p");
		if (hasP || opts.hasOpt("f")) {
			return printFuncs(opts, args);
		} else if (hasP || opts.hasOpt("v")) {
			return printVars(opts, args);
		} else if (hasP || opts.hasOpt("m")) {
			return printModules(opts, args);
		} else if (hasP || opts.hasOpt("n"))
			return printNamespaces(opts, args);

		else if (hasP || opts.hasOpt("x")) {
			export(opts.getOptStringRequired("x"));
			return 0;
		}
		if (!hasP && ! args.isEmpty()) {

			XValue what = args.remove(0);
			if (what.toString().equals("namespace")) {
				if( args.isEmpty() )
					return printNamespaces(opts, args);
				else
				return declareNamespace(args.get(0));
			}
		}

		usage();

		return 2;

	}

	private int printNamespaces(Options opts, List<XValue> args) {
		int ret = 0;
		if (args.isEmpty())
			ret = listNamespaces();
		else
			for (XValue a : args)
				ret += listNamespace(a.toString());
		return ret;

	}

	private int printVars(Options opts, List<XValue> args)
			throws UnsupportedEncodingException, CoreException, IOException,
			XMLStreamException, SAXException, SaxonApiException {
		if (args.size() < 1) {
			printAllVars();

		} else
			printVar(args.get(0).toString());

		return 0;
	}

	private int printFuncs(Options opts, List<XValue> args)
			throws UnsupportedEncodingException, CoreException, IOException {
		if (args.isEmpty())
			printAllFuncs();
		else
			printFunc(args.get(0).toString());
		return 0;
	}

	private int printAllVars() throws UnsupportedEncodingException,
			CoreException, IOException {

		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			for (String var : getShell().getEnv().getVarNames())
				w.println(var);
		}
		return 0;

	}

	private int printAllFuncs() throws UnsupportedEncodingException,
			CoreException, IOException {
		FunctionDefinitions fd = getShell().getFunctionDelcs();
		if (fd == null)
			return 0;
		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			for (Entry<String, IFunctionDecl> e : getShell().getEnv()
					.getFunctions().entrySet()) {

				w.printf("%s [%s]\n", e.getKey(), e.getValue().getModule()
						.getName());

			}
		}
		return 0;

	}

	private int printFunc(String name) throws UnsupportedEncodingException,
			CoreException, IOException {
		try (PrintWriter w = getStdout().asPrintWriter(getSerializeOpts())) {

			IFunctionDecl fd = getShell().getFunctionDecl(name);
			if (fd == null)
				w.println("Function: " + name + " not defined");
			else {

				w.print(name);
				w.println("()");
				w.println(fd.getBody().toString(false));

			}
		}
		return 0;

	}

	private int export(String varname) {
		XVariable xvar = mShell.getEnv().getVar(varname);
		if (xvar == null) {
			xvar = XVariable.newInstance(varname);
		}
		xvar.setFlag(XVarFlag.EXPORT);
		mShell.getEnv().setVar(xvar);
		return 0;

	}

	private int printVar(String name) throws XMLStreamException, SAXException,
			IOException, CoreException, SaxonApiException {
		XEnvironment env = mShell.getEnv();

		XVariable var = env.getVar(name);
		if (var == null) {
			this.printErr("Unknown variable: " + name);
			return 1;
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

		return 0;

	}

	private int declareNamespace(List<XValue> args) {
		if (args.size() == 0)
			return listNamespaces();

		for (XValue arg : args) {
			declareNamespace(arg);

		}
		return 0;

	}

	private int listNamespace(String name) {
		NameValueMap<String> ns = mShell.getEnv().getNamespaces();
		if (ns == null)
			return 0;
		String uri = ns.get(name);
		if (uri != null) {
			mShell.printOut(name + "=" + uri);
			return 0;
		} else
			mShell.printErr("Namespace not found: " + name);
		return 1;

	}

	private int listNamespaces() {
		NameValueMap<String> ns = mShell.getEnv().getNamespaces();
		if (ns == null)
			return 0;
		for (String name : ns.keySet()) {
			String uri = ns.get(name);
			mShell.printOut(name + "=" + uri);
		}
		return 0;

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

			return 0;

		} else
			usage("Unexpected value: " + arg.describe());
		return 2;
	}

	private int printModules(Options opts, List<XValue> args) {
		if (args.isEmpty()) {

			for (ModuleHandle hm : mShell.getEnv().getModules()) {
				printModule(hm);

			}
		} else {
			for (XValue a : args) {
				String name = a.toString();
				ModuleHandle hm = getEnv().getModules()
						.getExistingModuleByName(name);
				if (hm == null)
					printErr("Module not found: " + name);
				else
					printModule(hm);
			}
		}
		return 0;

	}

	protected void printModule(ModuleHandle hm) {
		XEnvironment env = getEnv();
		StringBuilder sb = new StringBuilder();
		String p = "";
		for (String prefix : env.getPrefixesForModule(hm)) {
			sb.append(p).append(prefix).append('=');
			p = " ";
		}

		String s = hm.get().describe();
		if (Util.isBlank(s))
			sb.append(hm.getName());
		else
			sb.append(s);

		mShell.printOut(sb.toString());
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
