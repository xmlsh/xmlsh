/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands;

import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;


public class xbase extends XCommand {

	private DocumentBuilder mBuilder;
	private Processor mProcessor;

	private void setupBuilders()
	{

		mProcessor = Shell.getProcessor();	

		mBuilder = mProcessor.newDocumentBuilder();
		
	}
	
	
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{

		
		
		Options opts = new Options( "i:" , args );
		opts.parse();
		
		setupBuilders();

		XdmNode	context = null;

		
		OptionValue ov = opts.getOpt("i");

			
		// If -i argument is an XML expression take the first node as the context
		if( ov != null  && ov.getValue().isXExpr() ){
			XdmItem item = ov.getValue().asXdmValue().itemAt(0);
			if( item instanceof XdmNode )
				context = (XdmNode) item;

		}
		
		/*
		 * If any remaining args use it as a URI
		 */
		
		if( context == null && opts.hasRemainingArgs() ){
			Source src = new StreamSource( opts.getRemainingArgs().get(0).toString());
			
			context = build(src);
			
		
		}
		
		if( context == null )
		{

			if( ov != null && ! ov.getValue().toString().equals("-"))
				context = build( getSource(ov.getValue()));
			else {
				context = build(getStdin().asSource());
			}	
		}

		

		PrintWriter out = getStdout().asPrintWriter();
		
		out.println( context.getBaseURI() );
	
		out.flush();
		
		
		return 0;

	}
	
	


	

	
	/*
	 * Creates/Builds a Tree (LINKED_TREE) type node from any source
	 */
	
	private XdmNode build( Source src ) throws SaxonApiException
	{

		return mBuilder.build( src);
		
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
