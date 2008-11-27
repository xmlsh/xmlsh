/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.commands.util.CSVFormatter;
import org.xmlsh.commands.util.CSVRecord;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;

/*
 * 
 * Convert XML files to an CSV file
 * 
 * Arguments
 * 
 * -header		Add a header row
 * 
 * 
 */

public class xml2csv extends XCommand
{

	private boolean bHeader = false ;
	private String mRowXpath 	 = "/*/*";
	private String mFieldXPath  = "*/string()";
	private String mHeaderXPath = "*/name()";
	private boolean bAttr = false ;
	
	
	private XQueryCompiler mCompiler;

	private CSVFormatter mFormatter;
	private OutputStream mOutput;
	





	public int run(  List<XValue> args  )	throws Exception
	{
		mFormatter = new CSVFormatter();
		mOutput = getStdout().asOutputStream();

		Options opts = new Options( "header,n,i:,attr" , args );
		opts.parse();
		
		bHeader = opts.hasOpt("header");
		bAttr = opts.hasOpt("attr");
		
		Processor processor = Shell.getProcessor();
		mCompiler = processor.newXQueryCompiler();
		XdmNode	context = null;

		DocumentBuilder builder = processor.newDocumentBuilder();
		
		// boolean bReadStdin = false ;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");

			
			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXExpr() ){
				XdmItem item = ov.getValue().toXdmValue().itemAt(0);
				if( item instanceof XdmNode )
				//   context = (XdmNode) item ; // builder.build(((XdmNode)item).asSource());
					context = builder.build(((XdmNode)item).asSource());
				 // context = (XdmNode) ov.getValue().toXdmValue();
			}
			if( context == null )
			{
	
				if( ov != null && ! ov.getValue().toString().equals("-"))
					context = builder.build( getFile(ov.getValue()));
				else {
					context =  getStdin().asXdmNode();
				}	
			}
		}
		

		// List<XValue> xvargs = opts.getRemainingArgs();
		if( bAttr ){
			mFieldXPath  = "for $a in @* order by $a/name() return $a/string()";
			mHeaderXPath = "for $a in @* order by $a/name() return $a/name()";
			
			
		}
		


		XQueryExecutable expr = mCompiler.compile( mRowXpath );
		
		XQueryEvaluator eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		
		
		boolean bFirst = true ;
		for( XdmItem row : eval ){
			if( bFirst && bHeader ){
				writeHeader(row);
				bFirst = false ;
			}
			writeLine(row, mFieldXPath );
			
			
			
			
		}
		return 0;
		
		
		
	}



	private void writeLine(XdmItem row, String xpath ) throws SaxonApiException, IOException {
		XQueryExecutable expr = mCompiler.compile( xpath );
		

		List<String> fields = new ArrayList<String>();
		
		XQueryEvaluator eval = expr.load();
		if( row != null )
			eval.setContextItem(row);
		
		for( XdmItem field : eval ){
			fields.add( field.toString());
			
		}
		CSVRecord rec = new CSVRecord(fields);
		String line = mFormatter.encodeRow(rec);
		mOutput.write( (line +  "\n") . getBytes());
		
		
		
	}



	private void writeHeader(XdmItem row) throws SaxonApiException, IOException 
	{
		writeLine(row,mHeaderXPath);
		
		
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
