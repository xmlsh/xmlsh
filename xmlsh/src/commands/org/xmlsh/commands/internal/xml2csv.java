/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.commands.util.CSVFormatter;
import org.xmlsh.commands.util.CSVRecord;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
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
		
	
		Options opts = new Options( "header,attr,delim:,quote:" , SerializeOpts.getOptionDefs() );
		opts.parse(args);
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		
		
		bHeader = opts.hasOpt("header");
		bAttr = opts.hasOpt("attr");
	
		
		
		String delim = opts.getOptString("delim", ",");
		String quote = opts.getOptString("quote", "\"");
		
		mFormatter = new CSVFormatter(delim.charAt(0),quote.charAt(0));
		mOutput = getStdout().asOutputStream(serializeOpts);

	
		
		Processor processor = Shell.getProcessor();
		mCompiler = processor.newXQueryCompiler();
		InputPort  in = getStdin();
		XdmNode	context = in.asXdmNode(serializeOpts);

		

		// List<XValue> xvargs = opts.getRemainingArgs();
		if( bAttr ){
			mFieldXPath  = "for $a in @* order by $a/name() return $a/string()";
			mHeaderXPath = "for $a in @* order by $a/name() return $a/name()";
			
			
		}
		


		XQueryExecutable expr = mCompiler.compile( mRowXpath );
		
		XQueryEvaluator eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		XQueryExecutable headerExpr = mCompiler.compile( mHeaderXPath );
		XQueryEvaluator headerEval = headerExpr.load();

		
		XQueryExecutable fieldExpr = mCompiler.compile( mFieldXPath );
		XQueryEvaluator fieldEval = fieldExpr.load();
		
		
		boolean bFirst = true ;
		for( XdmItem row : eval ){
			if( bFirst && bHeader ){
				writeHeader(row,headerEval);
				bFirst = false ;
			}
			writeLine(row, fieldEval );
			
		}
		return 0;
		
		
		
	}



	private void writeLine(XdmItem row,  XQueryEvaluator eval ) throws SaxonApiException, IOException {
		
		

		List<String> fields = new ArrayList<String>();
		
		
		if( row != null )
			eval.setContextItem(row);
		
		for( XdmItem field : eval ){
			fields.add( field.toString());
			
		}
		CSVRecord rec = new CSVRecord(fields);
		String line = mFormatter.encodeRow(rec);
		mOutput.write( (line +  "\n") . getBytes());
		
		
		
	}



	private void writeHeader(XdmItem row, XQueryEvaluator eval) throws SaxonApiException, IOException 
	{
		writeLine(row,eval);
		
		
	}

	
}

//
//
//Copyright (C) 2008-2012  David A. Lee.
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
