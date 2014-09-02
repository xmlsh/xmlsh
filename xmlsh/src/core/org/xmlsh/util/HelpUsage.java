/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class HelpUsage {

	private SerializeOpts mSerializeOpts;
	private Shell mShell;
	private static final String kHELP_XQUERY = "/org/xmlsh/resources/help/help.xquery";
	private static final String kCOMMANDS_XQUERY = "/org/xmlsh/resources/help/commands.xquery";
	private static final String kUSAGE_XQUERY = "/org/xmlsh/resources/help/usage.xquery";



	public HelpUsage(Shell shell) {

		mSerializeOpts = shell.getSerializeOpts();
		mShell = shell;
	}

	public void doHelpCommands(IOutputPort out, URL url, String prefix, String module, boolean bLaunch ) throws SaxonApiException, IOException, CoreException, URISyntaxException {

		XdmNode root = loadHelp(url);

		Processor  processor  = Shell.getProcessor();
		XQueryCompiler compiler = processor.newXQueryCompiler();

		InputStream isQuery = this.getClass().getResourceAsStream(kCOMMANDS_XQUERY);
		XQueryExecutable expr = compiler.compile(isQuery);
		isQuery.close();


		XQueryEvaluator eval = expr.load();
		eval.setExternalVariable(new QName("prefix"), XValue.newXValue(prefix).toXdmValue() );
		eval.setExternalVariable(new QName("module"), XValue.newXValue(module).toXdmValue() );

		eval.setContextItem(root);


		Destination dest = out.asDestination(mSerializeOpts);

		eval.run(dest);

		if( bLaunch )
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
	public void doHelp(IOutputPort stdout, String name, boolean bXml, boolean bLaunch ) throws IOException, URISyntaxException, SaxonApiException, CoreException {

		URL helpUrl = CommandFactory.getInstance().getHelpURL(mShell, name);
		if( helpUrl == null ){
			mShell.printErr("No help for " + name );
			return ;

		}



		// Get rid of namespace prefix from name
		StringPair p = new StringPair(name,':');
		String nonsName = p.getRight();


		XdmNode root = loadHelp( helpUrl );

		// Special case for : 
		if( name.equals(":"))
			nonsName = ":";



		XValue v = XValue.newXValue(root);
		XValue xcmd = v.xpath(mShell,"//(command|function)[@name='" + nonsName + "']");

		if( xcmd.isEmpty() ){
			mShell.printErr("No help for: " + name );
			return ;
		}



		if( bLaunch ){
			String uri = xcmd.xpath(mShell,"./@url/string()").toString();
			launchBrowser(uri);
		}

		if( bXml )
			printxml(xcmd.asXdmNode(),stdout) ;
		else
			print( xcmd.asXdmNode(), stdout,kHELP_XQUERY );

		stdout.writeSequenceTerminator(mSerializeOpts);




	}

	public void doUsage(IOutputPort stdout, String name ) throws IOException, URISyntaxException, SaxonApiException, CoreException {

		URL helpUrl = CommandFactory.getInstance().getHelpURL(mShell, name);
		if( helpUrl == null ){
			mShell.printErr("No usage for " + name );
			return ;

		}

		// Get rid of namespace prefix from name
		StringPair p = new StringPair(name,':');
		String nonsName = p.getRight();


		XdmNode root = loadHelp( helpUrl );


		// Special case for : 
		if( name.equals(":"))
			nonsName = ":";

		XValue v = XValue.newXValue(root);
		XValue xcmd = v.xpath(mShell,"//command[@name='" + nonsName + "']");

		if( xcmd.isEmpty() ){
			mShell.printErr("No help for: " + name );
			return ;
		}


		print( xcmd.asXdmNode(), stdout,kUSAGE_XQUERY );

		stdout.writeSequenceTerminator(mSerializeOpts);


	}
	private void launchBrowser(String uri) throws IOException, URISyntaxException {
		if(  uri != null ){
			Desktop desktop = 
					Desktop.isDesktopSupported() ? Desktop.getDesktop() : null ; // Avoid exception 

					if( desktop != null ) 
						desktop.browse(new URI(uri));

		}
	}

	private void printxml(XdmNode node, IOutputPort out) throws SaxonApiException, CoreException {

		Util.writeXdmValue(node, out.asDestination(this.mSerializeOpts));


	}

	private void print(XdmNode node,  IOutputPort out,String query) throws SaxonApiException, IOException, CoreException {

		Processor  processor  = Shell.getProcessor();
		XQueryCompiler compiler = processor.newXQueryCompiler();

		InputStream isQuery = this.getClass().getResourceAsStream(query);
		XQueryExecutable expr = compiler.compile(isQuery);
		isQuery.close();


		XQueryEvaluator eval = expr.load();
		eval.setContextItem(node);


		Destination dest = out.asDestination(mSerializeOpts);

		eval.run(dest);


	}

	public void doHelpCommands(IOutputPort stdout, boolean bLaunch) throws SaxonApiException, IOException, CoreException, URISyntaxException {		
		for( IModule m : mShell.getModules() ){
			URL url = m.getHelpURL();
			if( url != null )
				doHelpCommands(stdout,url,m.getPrefix(),m.getName(), bLaunch);
			bLaunch = false ;
		}

	}
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
