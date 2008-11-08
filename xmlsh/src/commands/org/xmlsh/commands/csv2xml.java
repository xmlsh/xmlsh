/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.commands.util.CSVParser;
import org.xmlsh.commands.util.CSVRecord;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

/*
 * 
 * Convert CSV files to an XML file
 * Arguments
 * 	-root		root element (default "root")
 *  -row		row	 element (default "row")
 *  -col		col	 element (defauilt "col")
 *  -header		read first row for header names
 *  -attr		write in attribute normal format
 */

public class csv2xml extends XCommand
{

	public static void main( String args[] ) throws Exception
	{
		csv2xml cmd = new csv2xml();

		cmd.run( args );
		
		
		
	}

	
	
	public int run(  List<XValue> args , XEnvironment env )	throws Exception
	{

		

		Options opts = new Options( "root:,row:,col:,header,attr,encoding:,delim:" , args );
		opts.parse();
		
		// root node
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		String col = opts.getOptString("col", "col");
		String delim = opts.getOptString("delim", ",");
		String encoding = opts.getOptString("encoding", "Cp1252");
		boolean bHeader = opts.hasOpt("header");
		boolean bAttr = opts.hasOpt("attr");
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
// Output XML
		OutputStream stdout = env.getStdout();
		TransformerHandler hd = Util.getTransformerHander(stdout);
		hd.startDocument();
		Attributes attrs = new AttributesImpl();
		hd.startElement("", root,root,attrs);
		
// Input is stdin and/or list of commands
		
		InputStream in = null;
		if( xvargs.size() == 0 || xvargs.get(0).toString().equals("-"))
			in = env.getStdin();
		else
			in = new FileInputStream(  env.getShell().getFile(xvargs.get(0)) );
		
		
		Reader ir = new InputStreamReader( in , encoding );
		CSVParser parser = new CSVParser( delim.charAt(0) );
		
		CSVRecord header = null ;
		if( bHeader ){
			String line = readLine(ir);
			if( line != null )
				header = parser.parseLine(line);
		}
		
		String line;
		while( (line = readLine(ir)) != null ){
			CSVRecord csv = parser.parseLine(line);
			addElement( hd , csv , row , col , bAttr , header );
		}
		hd.endElement("", root,root);
		hd.endDocument();
		
		ir.close();
		
		
		
		return 0;
		
	}



	private void addElement(
		TransformerHandler hd, 
		CSVRecord csv ,
		String row, 
		String col, 
		boolean battr,
		CSVRecord header) throws SAXException 
	{
		
		
		// Attribute normal format
		if( battr ){
			AttributesImpl attrs = new AttributesImpl();
			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getAttrName( i , col , header );
				attrs.addAttribute("", name , name, "CDATA", csv.getField(i));
			}
			hd.startElement("", row,row,attrs);
			hd.endElement("", row, row);
		} else {
			AttributesImpl attrs = new AttributesImpl();
			hd.startElement("", row,row,attrs);

			for( int i = 0 ; i < csv.getNumFields() ; i++ ){
				String name = getColName( i , col , header );
				hd.startElement("", name, name, attrs);
				char[] chars = csv.getField(i).toCharArray();
				hd.characters(chars,0,chars.length);
				hd.endElement("", name, name);
			}
			hd.endElement("", row,row);
		}
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
