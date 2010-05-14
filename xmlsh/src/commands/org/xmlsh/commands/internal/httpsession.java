/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXdmValueOutputStream;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.servlet.ManagedHttpSession;
import org.xmlsh.sh.shell.SerializeOpts;

public class httpsession extends XCommand {



	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = new Options( "getvar:,setvar:,n", SerializeOpts.getOptionDefs()  );
		opts.parse(args);
		

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		
		String 	getVar = opts.getOptString("getvar", null);
		String  setVar = opts.getOptString("setvar", null);
		Boolean noErr  = opts.hasOpt("n");
		if( getVar == null && setVar == null){
			usage();
			return 1;
		}
		
		ManagedHttpSession msess = (ManagedHttpSession) getShell().getSession().getVar("HTTP_SESSION");
		if( msess == null ){
			printErr("HTTP_SESSION not found");
			return 2;
		}
		try {
			
		
			
			if( getVar != null )
			{
				Object value = msess.getSession().getAttribute(getVar);
				if( value != null ){
					writeValue( value , serializeOpts );
					return 0;
				}
				else
				if( ! noErr )
					printErr("No session variable: " + getVar );
				
				return 1;
				
				
			} else
			if( setVar != null )
			{
				args = opts.getRemainingArgs();
				if( args.size() < 1 ){
					usage();
					return 1;
				}
				msess.getSession().setAttribute(setVar , args.get(0));
				return 0;
			}

		} finally {
			msess.release();
		}
		// SNH
		return 0;
	}


	private void writeValue(Object value, SerializeOpts serializeOpts) throws CoreException, IOException {
		if( value instanceof XValue )
		{
			XValue xv = (XValue) value ;
			IXdmValueOutputStream dest =  getStdout().asXdmValueOutputStream(serializeOpts);

			dest.write(xv.asXdmValue());
			

		} else
		{
			String svalue = value.toString();
			getStdout().asPrintStream(serializeOpts).print(svalue);
			
		}
		
	}

}



//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
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
