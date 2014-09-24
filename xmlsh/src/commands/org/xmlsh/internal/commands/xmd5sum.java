/**
 * $Id: xfile.java 356 2010-01-01 15:46:31Z daldei $
 * $Date: 2010-01-01 10:46:31 -0500 (Fri, 01 Jan 2010) $
 *
 */

package org.xmlsh.internal.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;
import org.xmlsh.util.commands.Checksum;


public class xmd5sum extends XCommand
{


	private static final  String sFile = "file";
	private static final	String sName = "name";
	private static final 	String sMd5 = "md5";
	private static final   String sLen = "length";
	private static String sDocRoot = "xmd5";


	@Override
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("b=binary,x=xml",SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		XMLStreamWriter 	out = null ;
		OutputPort stdout = mShell.getEnv().getStdout();	

		SerializeOpts serializeOpts = getSerializeOpts(opts);


		out = stdout.asXMLStreamWriter(serializeOpts);
		out.writeStartDocument();
		out.writeStartElement(sDocRoot);



		if( args.isEmpty() )
			args.add(XValue.newXValue("-"));



		for( XValue arg : args ){

			writeMD5(arg, out, serializeOpts);


		}
		out.writeEndElement();
		out.writeEndDocument();
		out.flush();
		out.close();

		stdout.writeSequenceTerminator(serializeOpts);

		return 0;


	}


	private void writeMD5(XValue arg, XMLStreamWriter out, SerializeOpts serializeOpts)
			throws CoreException, IOException, XMLStreamException 
			{


		/*
		 * Attempt to safely recurse if a file/directory
		 */
		String sArg = arg.toString();
		URL url = Util.tryURL(sArg);
		if( url == null ){
			File file = mShell.getFile(arg);
			if( file != null && file.exists() && file.isDirectory() ){
				for( String child : file.list() )
					// Use / not File.seperator so that we keep java paths 
					writeMD5( XValue.newXValue(sArg + "/" + child),
							out , 
							serializeOpts
							);

				return ;
			}


		}

		InputPort inp= getInput(arg);
		try (
				InputStream in = inp.asInputStream(serializeOpts) ){
			Checksum cs = Checksum.calcChecksum(in);
			out.writeStartElement(sFile);
			String name = arg.toString();
			if( !Util.isBlank(name) )
				out.writeAttribute(sName, FileUtils.toJavaPath(name));
			out.writeAttribute(sMd5, cs.getMD5());
			out.writeAttribute(sLen , String.valueOf( cs.getLength()) );
			out.writeEndElement();


		} 


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
