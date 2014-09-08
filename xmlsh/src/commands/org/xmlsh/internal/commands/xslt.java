/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.internal.commands;

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
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellURIResolver;

public class xslt extends XCommand {

	private class myErrorListener implements ErrorListener {

		@Override
		public void error(TransformerException exception) throws TransformerException {
			printErr("Error: " + exception.getMessageAndLocation());

		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException {
			printErr("Fatal Error: " + exception.getMessageAndLocation());


		}

		@Override
		public void warning(TransformerException exception) throws TransformerException {
			printErr("Warning : " + exception.getMessageAndLocation());

		}

	}

	private class myMessageListener implements MessageListener {

		@Override
		public void message(XdmNode content, boolean terminate, SourceLocator locator) {
			printErr(content.getStringValue());

		}

	}

	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options("c=context:,cf=context-file:,f=file:,i=input:,n,v",SerializeOpts.getOptionDefs());
		opts.parse(args);

		Processor processor = Shell.getProcessor();

		XsltCompiler compiler = processor.newXsltCompiler();
		compiler.setErrorListener(new myErrorListener());
		Source context = null;

		InputPort in = null;


		// Use a copy of the serialize opts so we can override the method 
		SerializeOpts serializeOpts = getSerializeOpts(opts);






		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			// Order of prevelence 
			// -context
			// -context-file
			// -i

			if( opts.hasOpt("c") )
				context = opts.getOptValue("c").asSource();
			else
				if( opts.hasOpt("cf"))
					context = (in=getInput( XValue.newXValue(opts.getOptString("cf", "-")))).asSource(serializeOpts);
				else
					if( opts.hasOpt("i") )
						context = (in=getInput( opts.getOptValue("i"))).asSource(serializeOpts);
					else
						context = (in=getStdin()).asSource(serializeOpts);

		}

		Source source = null;
		InputPort closePort = null ;
		List<XValue> xvargs = opts.getRemainingArgs();

		OptionValue ov = opts.getOpt("f");
		if (ov != null) {
			closePort = getInput(ov.getValue());
			if( closePort != null)
				source = closePort.asSource(serializeOpts);
		}

		if (source == null) {
			throwInvalidArg("No xslt source specified");
		}

		/*
		 * Add namespaces -- DOESNT WORK FOR XSLT
		 *  { NameValueMap<String> ns = getEnv().getShell().getNamespaces();
		 * if( ns != null ){ for( String prefix : ns.keySet() ){ String uri =
		 * ns.get(prefix); compiler.declareNamespace(prefix, uri);
		 *  }
		 *  } }
		 */

		XsltExecutable expr = compiler.compile(source);

		compiler.setURIResolver(new ShellURIResolver(compiler.getURIResolver()));
		XsltTransformer eval = expr.load();
		eval.setMessageListener(new myMessageListener());

		if (context != null)
			eval.setSource(context);

		if (opts.hasOpt("v")) {
			// Read pairs from args to set
			for (int i = 0; i < xvargs.size() / 2; i++) {
				String name = xvargs.get(i * 2).toString();
				XValue value = xvargs.get(i * 2 + 1);

				eval.setParameter(new QName(name), value.toXdmValue());
			}
		}

		OutputPort stdout = getStdout();
		eval.setDestination(stdout.asDestination(serializeOpts));

		eval.transform();
		stdout.writeSequenceTerminator(serializeOpts);

		return 0;

	}

}

// Copyright (C) 2008-2014    David A. Lee.

// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php

// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.

// The Original Code is: all this file.

// The Initial Developer of the Original Code is David A. Lee

// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.

// Contributor(s): none.

