/**
 * $Id: echo.java 88 2008-11-27 17:06:00Z daldei $
 * $Date: 2008-11-27 12:06:00 -0500 (Thu, 27 Nov 2008) $
 *
 */

package org.xmlsh.commands.builtin;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class log extends BuiltinCommand {
	
	public int run( List<XValue> args ) throws Exception {

		Options opts = new Options( "c=class:,p=priority:"  );
		opts.parse(args);
		

		String sClass = opts.getOptString("c", log.class.getName());
		String sLevel = opts.getOptString("p" , "info");
		
		Priority priority = parseLevel(sLevel);
		
		
		args = opts.getRemainingArgs();

		/* Serialize all output into a single string
		 * 
		 */
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		args = Util.expandSequences( args);
		boolean bFirst = true;
		for ( XValue arg : args ){
				if( ! bFirst )
					out.write(' ');
				
				bFirst = false;
				arg.serialize( out , getSerializeOpts() );
		}

		out.flush();
		
		Logger logger = Logger.getLogger(sClass);
		
		logger.log(priority, out.toString(getSerializeOpts().getOutputTextEncoding()));
		
		
		
		return 0;
	}

	private Priority parseLevel(String sLevel) {
		
		return Priority.toPriority(sLevel,Priority.INFO);
	}
}
//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
