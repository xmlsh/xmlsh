package org.xmlsh.commands.builtin;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

/**
 * $Id: $
 * $Date: $
 *
 */

public class help extends BuiltinCommand {

	
	private static final String kHELP_XQUERY = "/org/xmlsh/resources/help/help.xquery";
	private boolean mNoBrowser = false;
	
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( "h=help,n=nobrowse" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		 mNoBrowser = opts.hasOpt("n");
		
		
		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());
		
		if( xvargs.size() < 1  ){
			usage();
		}
		
		OutputPort stdout = mShell.getEnv().getStdout();
		PrintWriter out = stdout.asPrintWriter(this.getSerializeOpts(opts));
		
		for( XValue xname : xvargs ){
			
			String name = xname.toString();
			doHelp(out,name);
			
			
		}
		
		
		
		
		out.close();
		
		return 0;
		
		
		
	}

	private void doHelp(PrintWriter out, String name) throws IOException, URISyntaxException, SaxonApiException, UnexpectedException {
		
		URL helpUrl = CommandFactory.getInstance().getHelpURL(mShell, name);
		if( helpUrl == null ){
			out.println("No help for " + name );
			return ;
			
		}
		
		// Get rid of namespace prefix from name
		StringPair p = new StringPair(name,':');
		String nonsName = p.getRight();
		
		
		DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		InputStream is = helpUrl.openStream();
		try {
			
			Source source = new StreamSource( is );
			XdmNode root = builder.build(source);
			
			XValue v = new XValue(root);
			XValue uri = v.xpath("//command[@name='" + nonsName + "']/@url/string()");
			if( !mNoBrowser &&  uri != null ){
				Desktop desktop = 
					Desktop.isDesktopSupported() ? Desktop.getDesktop() : null ; // Avoid exception 
				
				if( desktop != null ) 
					desktop.browse(new URI(uri.toString()));
				
			}
			print( root , nonsName , out );
			out.println();

		} finally {
			is.close();
		}

		
	}

	private void print(XdmNode node, String command, PrintWriter out) throws SaxonApiException, IOException {
		
		Processor  processor  = Shell.getProcessor();
		XQueryCompiler compiler = processor.newXQueryCompiler();
		
		InputStream isQuery = this.getClass().getResourceAsStream(kHELP_XQUERY);
		XQueryExecutable expr = compiler.compile(isQuery);
		isQuery.close();
		
		
		XQueryEvaluator eval = expr.load();
		eval.setExternalVariable(new QName("command"), new XValue( command).asXdmValue()  );
		eval.setContextItem(node);
		Serializer dest = Util.getSerializer(mShell.getSerializeOpts());
		dest.setOutputWriter(out);	
		eval.run(dest);
		
		
		
		
		
		
		
	}

	private void usage() {
		// TODO Auto-generated method stub
		
	}
	
}



//
//
//Copyright (C) 2008,2009 David A. Lee.
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
