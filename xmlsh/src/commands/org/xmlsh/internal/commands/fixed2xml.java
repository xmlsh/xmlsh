/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.internal.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.xmlsh.util.commands.CSVRecord;
import org.xmlsh.util.commands.FixedParser;

/*
 * 
 * Convert fixed field width files to an XML file
 * Arguments
 * 	-root		root element (default "root")
 *  -row		row	 element (default "row")
 *  -col		col	 element (defauilt "col")
 *  -colspecs   specs sequence or , delimited list of column specs
 *  -colnames	<seq> or , seperated list  Column names instead of reading from header
 *  -header		read first row for header names
 *  -attr		write in attribute normal format
 *  -encoding encoding  Read CSV format in the specified encoding, else cp1252 assumed
 *  -quote		quoted by (default ")
 */

public class fixed2xml extends XCommand
{


	@Override
	public int run(  List<XValue> args )	throws Exception
	{



		Options opts = new Options( "root:,row:,col:,header,attr,encoding:,colnames:,colspecs:,nonorm=nonormalize,skip:",SerializeOpts.getOptionDefs());
		opts.parse(args);

		// root node
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		String col = opts.getOptString("col", "col");
		String encoding = opts.getOptString("encoding", "Cp1252");
		boolean bAttr = opts.hasOpt("attr");
		boolean bHeader = opts.hasOpt("header");
		boolean bNoNorm = opts.hasOpt("nonorm");
		int 	 skip 	 = opts.getOptInt("skip", 0);


		List<XValue> xvargs = opts.getRemainingArgs();

		// Output XML

		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);

		writer.writeStartDocument();


		writer.writeStartElement(root);

		// Input is stdin and/or list of commands

		InputStream in = null;
		if( xvargs.size() == 0 )
			in = getStdin().asInputStream(serializeOpts);
		else
			in = getInputStream( xvargs.get(0) );


		Reader ir = new InputStreamReader( in , encoding );
		FixedParser parser = new FixedParser( parseWidths( opts.getOptValueRequired("colspecs")) , ! bNoNorm);

		CSVRecord header = null ;

		while( skip-- > 0 )
			readLine(ir);


		if( bHeader ){
			String line = readLine(ir);
			if( line != null )
				header = parser.parseLine(line);
		} else 
			if( opts.hasOpt("colnames")){
				header = parseCols( opts.getOptValue("colnames"));
			}

		String line;
		while( (line = readLine(ir)) != null ){
			CSVRecord csv = parser.parseLine(line);
			addElement( writer , csv , row , col , bAttr , header );
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();

		ir.close();
		stdout.writeSequenceTerminator(serializeOpts);


		return 0;

	}


	/*
	 * Parse either a sequence of column names, or a "," seperated list of column names
	 */

	private CSVRecord parseCols(XValue cols) {

		if( cols.isAtomic() )
			return new CSVRecord( Arrays.asList( cols.toString().split(",")));
		else
			return new CSVRecord(cols.asStringList());


	}




	private String[] parseWidths(XValue specs) {

		if( specs.isAtomic() )
			return specs.toString().split(",");



		List<String> list = specs.asStringList();

		return list.toArray( new String[list.size()]);


	}



	private void addElement(
			XMLStreamWriter writer, 
			CSVRecord csv ,
			String row, 
			String col, 
			boolean battr,
			CSVRecord header) throws  XMLStreamException 
			{

		writer.writeStartElement(row);
		// Attribute normal format
		if( battr ){
			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getAttrName( i , col , header );
				writer.writeAttribute(name,csv.getField(i));
			}


		} else {


			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getColName( i , col , header );
				writer.writeStartElement(name);
				writer.writeCharacters(csv.getField(i));
				writer.writeEndElement();

			}

		}
		writer.writeEndElement();

			}


	private String getColName(int i, String col, CSVRecord header) {
		if( header != null && header.getNumFields() > i )
			return toXmlName( header.getField(i));
		else
			return col ;
	}



	// Get an attribute name 
	private String getAttrName(int i, String col, CSVRecord header) {
		if( header != null && header.getNumFields() > i )
			return toXmlName( header.getField(i));
		else
			return col + (i + 1)  ;

	}



	private String toXmlName(String field) {
		return field.replaceAll("[^a-zA-Z0-9_]","-");
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
