/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.internal.commands;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;
import org.xmlsh.util.commands.CSVParser;
import org.xmlsh.util.commands.CSVRecord;

/*
 * 
 * Convert CSV files to an XML file
 * Arguments
 * 	-root		root element (default "root")
 *  -row		row	 element (default "row")
 *  -col		col	 element (defauilt "col")
 *  -colnames		<seq>   Column names instead of reading from header
 *  -header		read first row for header names
 *  -attr		write in attribute normal format\
 *  -encoding encoding  Read CSV format in the specified encoding, else cp1252 assumed
 *  -quote		quoted by (default ")
 *  -skip lines	Skip # lines before reading header or data ||
 *  -trim		Trim the output by ignoring any data after the last column specified in -colnames or -header
 *  -max 	#	Combine the data in the last field by ignoring any more delimiters after the # of fields is specifed, or if # is <=0 then use the number of colums
 */

public class csv2xml extends XCommand
{


	@Override
	public int run(  List<XValue> args )	throws Exception
	{



		Options opts = new Options( "root:,row:,col:,header,attr,delim:,quote:,colnames:,tab,skip:,trim,max:", SerializeOpts.getOptionDefs() );
		opts.parse(args);
		setSerializeOpts(opts);

		// root node
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		String col = opts.getOptString("col", "col");
		String delim = opts.getOptString("delim", ",");
		String quote = opts.getOptString("quote", "\"");
		boolean bHeader = opts.hasOpt("header");
		boolean bAttr = opts.hasOpt("attr");
		int	skip = Util.parseInt(opts.getOptString("skip", "0"),0);
		boolean bTrim = opts.hasOpt("trim"); 
		int maxFields = Util.parseInt(opts.getOptString("max", "0"),0);

		// -tab overrides -delim
		if( opts.hasOpt("tab"))
			delim = "\t";


		List<XValue> xvargs = opts.getRemainingArgs();




		// Output XML

		OutputPort stdout = getStdout();


		XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());

		writer.writeStartDocument();


		writer.writeStartElement(root);

		// Input is stdin and/or list of commands

		Reader ir = null;
		if( xvargs.size() == 0 )
			ir = getStdin().asReader(getSerializeOpts());
		else
			ir = getInput( xvargs.get(0) ).asReader(getSerializeOpts());


		CSVParser parser = new CSVParser( delim.charAt(0), quote.charAt(0) , maxFields );

		while( skip-- > 0 )
			readLine(ir);


		CSVRecord header = null ;
		if( bHeader ){
			String line = readLine(ir);
			if( line != null )
				header = parser.parseLine(line);
		} 

		// Even if bHeader override the colnames
		if( opts.hasOpt("colnames")){
			header = parseCols( opts.getOptValue("colnames"));
		}



		String line;
		while( (line = readLine(ir)) != null ){
			CSVRecord csv = parser.parseLine(line);
			addElement( writer , csv , row , col , bAttr , header,bTrim );
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();

		ir.close();
		stdout.writeSequenceTerminator(getSerializeOpts());


		return 0;

	}



	private CSVRecord parseCols(XValue cols) {

		if( cols.isAtomic() )
			return new CSVRecord( Arrays.asList( cols.toString().split(",")));
		else
			return new CSVRecord(cols.asStringList());


	}




	private void addElement(
			XMLStreamWriter writer, 
			CSVRecord csv ,
			String row, 
			String col, 
			boolean battr,
			CSVRecord header, 
			boolean bTrim
			) throws  XMLStreamException 
			{

		writer.writeStartElement(row);
		// Attribute normal format
		if( battr ){
			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getAttrName( i , col , header,bTrim);
				if( name != null )
					writer.writeAttribute(name,csv.getField(i));
			}


		} else {


			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getColName( i , col , header,bTrim );
				if( name != null ){
					writer.writeStartElement(name);
					writer.writeCharacters(csv.getField(i));
					writer.writeEndElement();
				}

			}

		}
		writer.writeEndElement();

			}


	private String getColName(int i, String col, CSVRecord header, boolean bTrim ) {
		if( header != null  ){
			if( header.getNumFields() <= i )
				return bTrim ? null : col ;

			return toXmlName( header.getField(i));
		}
		else
			return col ;
	}



	// Get an attribute name 
	private String getAttrName(int i, String col, CSVRecord header, boolean bTrim ) {
		if( header != null ){
			if( header.getNumFields() <= i )
				return bTrim ? null : col + (i + 1) ;

			return toXmlName( header.getField(i));
		}

		else
			return col + (i + 1)  ;

	}



	private String toXmlName(String field) {
		return Util.encodeForNCName(field);
	}



	private String readLine(Reader ir) throws IOException {
		return Util.readLine(ir);
	}

}

//
//
//Copyright (C) 2008-2014    David A. Lee.
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
