/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.modules.json;

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.json.JXConverter;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

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

public class xml2json extends XCommand
{


	@Override
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("f=format:",SerializeOpts.getOptionDefs());

		opts.parse(args);

		String format = opts.getOptString("format", "jxon");

		args = opts.getRemainingArgs();

		OutputPort stdout = getStdout();

		InputPort inp = args.isEmpty() ? getStdin() : getInput( args.get(0) );

		setSerializeOpts(opts);
		XMLStreamReader reader = inp.asXMLStreamReader(getSerializeOpts());

		try ( 
				// Override the text encoding to UTF-8 - JSON is *always* USTF8
				OutputStream os = stdout.asOutputStream(getSerializeOpts());
				){
			JXConverter converter = JXConverter.getConverter(format,getSerializeOpts(), args);

			converter.convertToJson( reader , os );
			return 0;

		} 
		finally {
			Util.safeClose(reader);
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
