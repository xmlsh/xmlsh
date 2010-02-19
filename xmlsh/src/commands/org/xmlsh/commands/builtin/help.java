package org.xmlsh.commands.builtin;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Module;
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
	private static final String kCOMMANDS_XQUERY = "/org/xmlsh/resources/help/commands.xquery";

	
	private boolean mNoBrowser = false;
	private boolean mXml = false ;
	private SerializeOpts mSerializeOpts;
	
	
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( "h=help,n=nobrowse,xml" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		 mNoBrowser = opts.hasOpt("n");
		 mXml = opts.hasOpt("xml");
		mSerializeOpts = mShell.getSerializeOpts(opts);
		
		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());
		

		
		String name =null; 
		if( xvargs.size() != 1  ){
			name = "help";
		} else
			name = 	xvargs.get(0).toString();
		
		OutputPort stdout = mShell.getEnv().getStdout();
		
		
		
		
		if( name.equals("commands"))
			doHelpCommands(stdout);
		else
			doHelp(stdout,name);
			
		stdout.release();
		
		return 0;
		
		
		
	}

	private void doHelpCommands(OutputPort stdout) throws SaxonApiException, IOException, CoreException, URISyntaxException {		for( Module m : mShell.getModules() ){
			URL url = m.getHelpURL();
			if( url != null )
				doHelpCommands(stdout,url,m.getPrefix(),m.getName());
		}
		
	}

	private void doHelpCommands(OutputPort out, URL url, String prefix, String module ) throws SaxonApiException, IOException, CoreException, URISyntaxException {
		
		XdmNode root = loadHelp(url);
		
		Processor  processor  = Shell.getProcessor();
		XQueryCompiler compiler = processor.newXQueryCompiler();
		
		InputStream isQuery = this.getClass().getResourceAsStream(kCOMMANDS_XQUERY);
		XQueryExecutable expr = compiler.compile(isQuery);
		isQuery.close();
		
		
		XQueryEvaluator eval = expr.load();
		eval.setExternalVariable(new QName("prefix"), new XValue(prefix).asXdmValue() );
		eval.setExternalVariable(new QName("module"), new XValue(module).asXdmValue() );
	
		eval.setContextItem(root);
		
		
		Destination dest = out.asDestination(mSerializeOpts);
	
		eval.run(dest);
		
		launchBrowser("http://www.xmlsh.org/Commands");
		
	}

	private XdmNode loadHelp(URL url) throws IOException, SaxonApiException{
		
		DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		InputStream is = url.openStream();
		try {
			
			Source source = new StreamSource( is );
			XdmNode root = builder.build(source);
			return root ;
		} finally {
			is.close();
		}
	}
	private void doHelp(OutputPort stdout, String name) throws IOException, URISyntaxException, SaxonApiException, CoreException {
		
		URL helpUrl = CommandFactory.getInstance().getHelpURL(mShell, name);
		if( helpUrl == null ){
			mShell.printErr("No help for " + name );
			return ;
			
		}
		
		
		
		// Get rid of namespace prefix from name
		StringPair p = new StringPair(name,':');
		String nonsName = p.getRight();
		
		
		XdmNode root = loadHelp( helpUrl );
		
				
			
			XValue v = new XValue(root);
			XValue xcmd = v.xpath("//command[@name='" + nonsName + "']");
			
			
			
			
			
			String uri = xcmd.xpath("./@url/string()").toString();
			launchBrowser(uri);
			
			if( mXml )
				printxml(xcmd.asXdmNode(),stdout) ;
			else
				print( xcmd.asXdmNode(), stdout );
			
			stdout.writeSequenceTerminator(mSerializeOpts);
			


		
	}

	private void launchBrowser(String uri) throws IOException, URISyntaxException {
		if( !mNoBrowser &&  uri != null ){
			Desktop desktop = 
				Desktop.isDesktopSupported() ? Desktop.getDesktop() : null ; // Avoid exception 
			
			if( desktop != null ) 
				desktop.browse(new URI(uri));
			
		}
	}

	private void printxml(XdmNode node, OutputPort out) throws SaxonApiException, CoreException {
		Util.writeXdmValue(node, out.asDestination(this.mSerializeOpts));

		
	}

	private void print(XdmNode node,  OutputPort out) throws SaxonApiException, IOException, CoreException {
		
		Processor  processor  = Shell.getProcessor();
		XQueryCompiler compiler = processor.newXQueryCompiler();
		
		InputStream isQuery = this.getClass().getResourceAsStream(kHELP_XQUERY);
		XQueryExecutable expr = compiler.compile(isQuery);
		isQuery.close();
		
		
		XQueryEvaluator eval = expr.load();
		eval.setContextItem(node);
		
		
		Destination dest = out.asDestination(mSerializeOpts);
	
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
