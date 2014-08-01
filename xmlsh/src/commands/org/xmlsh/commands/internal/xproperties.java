/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StringPair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/*
 * 
 * Manage a Java properties file
 */

public class xproperties extends XCommand 
{


	@Override
	public int run(  List<XValue> args )	throws Exception
	{



		Options opts = new Options( "in:,inxml:,text,xml,d=delete:+,v=var:+,a=add:+,c=comment:" , SerializeOpts.getOptionDefs()  );
		opts.parse(args);
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XValue optIn 		= opts.getOptValue("in");
		XValue optInXml		= opts.getOptValue("inxml");



		boolean	 bOutText = opts.hasOpt("text");



		if( optIn != null && optInXml != null ){
			usage("Only one of -in and -inxml allowed");
			return -1;
		}






		String comment = opts.getOptString("c", null);




		Properties props = new Properties();

		if( optInXml != null  )
			props.loadFromXML(getInput(optInXml).asInputStream(serializeOpts));
		else
			if( optIn != null )
				props.load(getInput(optIn).asInputStream(serializeOpts));


		/*
		 * Delete values as specified
		 */
		if( opts.hasOpt("d"))
			for (XValue d : opts.getOpt("d").getValues())
				props.remove(d.toString());

		List<String> printVars = null ;
		if( opts.hasOpt("v")){
			printVars = new ArrayList<String>();
			for (XValue var : opts.getOpt("v").getValues())
				printVars.add(var.toString() );
		}



		// Add value 

		if (opts.hasOpt("a")) {
			for (XValue add : opts.getOpt("a").getValues()){
				StringPair pair = new StringPair( add.toString() , '=');
				props.setProperty( pair.getLeft(), pair.getRight() );
			}
		}


		if( printVars != null )
			writeVars( props, printVars , serializeOpts);
		else
			if( ! bOutText)
				writeXML(props, comment);
			else
				writeText(props,comment,serializeOpts);




		return 0;

	}

	private void writeVars(Properties props, List<String> vars , SerializeOpts serializeOpts) throws UnsupportedEncodingException, IOException, CoreException {

		PrintWriter out = getStdout().asPrintWriter(serializeOpts);
		for( String var : vars )
			out.println( props.getProperty(var, "") );

		out.flush();


	}



	private void writeText(Properties props, String comment , SerializeOpts serializeOpts) throws IOException, CoreException {
		props.store(getEnv().getStdout().asOutputStream(serializeOpts), comment);

	}


	private void writeXML(Properties props, String comment)
			throws IOException, CoreException, SaxonApiException, XMLStreamException {

		SerializeOpts serializeOpts = getSerializeOpts();

		/*
		 * Load XML text into a buffer
		 */

		ByteArrayOutputStream oss = new ByteArrayOutputStream();
		props.storeToXML( oss , comment , serializeOpts.getOutputXmlEncoding());
		ByteArrayInputStream iss = new ByteArrayInputStream( oss.toByteArray());

		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.valueOf(false)); // Dont try to reference http://java.sun.com/dtd/properties.dtd !!!
		XMLEventReader reader = factory.createXMLEventReader( null , iss);
		XMLEventWriter writer = getStdout().asXMLEventWriter(serializeOpts);
		writer.add(reader);
		reader.close();
		writer.close();


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
