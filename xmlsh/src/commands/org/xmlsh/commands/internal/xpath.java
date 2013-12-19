/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class xpath extends XCommand {

	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options("c=context:,cf=context-file:,f=file:,i=input:,q=query:,n,v,e=exists,b=bool,nons,ns:+,s=string", SerializeOpts.getOptionDefs());
		opts.parse(args);

		
		boolean bString = 	opts.hasOpt("s");
		
		Processor processor = Shell.getProcessor();

		XPathCompiler compiler = processor.newXPathCompiler();
		XdmItem context = null;
		InputPort in = null;

		// boolean bReadStdin = false ;
		
		mSerializeOpts = getSerializeOpts(opts);
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			// Order of prevelence 
			// -context
			// -context-file
			// -i
			
			if( opts.hasOpt("c") )
				context = opts.getOptValue("c").asXdmItem();
			else
			if( opts.hasOpt("cf"))
				context = (in=getInput( new XValue(opts.getOptString("cf", "-")))).asXdmItem(mSerializeOpts);
			else
			if( opts.hasOpt("i") )
				context = (in=getInput( opts.getOptValue("i"))).asXdmItem(mSerializeOpts);
			else
				context = (in=getStdin()).asXdmItem(mSerializeOpts);
			
		}

		List<XValue> xvargs = opts.getRemainingArgs();

		boolean bQuiet = opts.hasOpt("e");
		boolean bBool = opts.hasOpt("b");
		if (bBool)
			bQuiet = true;

		OptionValue ov = opts.getOpt("f");
		String xpath = null;
		if (ov != null)
			xpath = readString(ov.getValue(),mSerializeOpts);
		else {
			ov = opts.getOpt("q");
			if (ov != null)
				xpath = ov.getValue().toString();
		}

		if (xpath == null)
			xpath = xvargs.remove(0).toString();

		if (opts.hasOpt("v")) {
			// Read pairs from args to set
			for (int i = 0; i < xvargs.size() / 2; i++) {
				String name = xvargs.get(i * 2).toString();

				compiler.declareVariable(new QName(name));

			}

		}

		/*
		 * Add namespaces If -nons option then dont use global namespaces If -ns
		 * options then add additional namespaces
		 */

		Namespaces ns = null;

		if (!opts.hasOpt("nons"))
			ns = getEnv().getNamespaces();
		if (opts.hasOpt("ns")) {
			Namespaces ns2 = new Namespaces();
			if (ns != null)
				ns2.putAll(ns);

			// Add custom name spaces
			for (XValue v : opts.getOpt("ns").getValues())
				ns2.declare(v);

			ns = ns2;
		}

		if (ns != null) {
			for (String prefix : ns.keySet()) {
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);

			}

		}

	
		
			XPathExecutable expr = compiler.compile(xpath);

			XPathSelector eval = expr.load();
			if (context != null)
				eval.setContextItem(context);

			if (opts.hasOpt("v")) {
				// Read pairs from args to set
				for (int i = 0; i < xvargs.size() / 2; i++) {
					String name = xvargs.get(i * 2).toString();
					XValue value = xvargs.get(i * 2 + 1);

					eval.setVariable(new QName(name), value.asXdmValue());
				}
			}
			// Return the effective boolean value intead of any output
			if (bBool) {
				boolean bRet = eval.effectiveBooleanValue();
				return bRet ? 0 : 1 ;
				

			} else {

				OutputPort stdout = getStdout();
				IXdmItemOutputStream ser = stdout.asXdmItemOutputStream(mSerializeOpts);
				boolean bAnyOutput = false;
				boolean bFirst = true;

				for (XdmItem item : eval) {
					bAnyOutput = true;

					if (bQuiet)
						break;

					if (!bFirst)
						stdout.writeSequenceSeperator(mSerializeOpts); // Thrashes variable
															// output !
					else {
						if (item instanceof XdmNode) {
							URI uri = ((XdmNode) item).getBaseURI();
							stdout.setSystemId(uri.toString());
						}
					}
					bFirst = false;
					if( item instanceof XdmNode ){
						XdmNode node = (XdmNode) item ;
						if( bString  )
							item = new XdmAtomicValue( node.getStringValue());
						
					}
					
					// Util.writeXdmValue(item, ser);
					ser.write(item);

				}
				if (!bQuiet && bAnyOutput)
					stdout.writeSequenceTerminator(mSerializeOpts);

				if( in != null )
					in.close();
				return bAnyOutput ? 0 : 1;
			}

	

	}

	private String readString(XValue v, SerializeOpts opts) throws CoreException, IOException  {
		
		InputPort in = getInput( v );
		InputStream is = in.asInputStream(opts);
		
		String s = Util.readString(is,opts.getInputTextEncoding());
		is.close();
		in.close();
		return s ;
	}

}

//
//
// Copyright (C) 2008-2014    David A. Lee.
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
