/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class rmdir extends XCommand {

	private File getFile( XValue arg , boolean bForce ) throws IOException
	{
		try {
			File f = getFile(arg);
			return f;
		} catch( IOException e )
		{
			// Ignore IO exceptions 
			if( ! bForce )
				throw e ;
			
		}
		return null ;
	}
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( ""  );
		opts.parse(args);
		args = opts.getRemainingArgs();
		int ret = 0;
		for( XValue arg : args ){
			File f = getFile( arg );

			if( f != null )
				ret += delete(f );
			
		}
		
		
		return ret;
	}

	private int delete(File f) {
		
		if( f.exists() ){
			if( f.isDirectory() ){
				boolean ok = f.delete();
				if( ! ok ){
					printErr("Error deleting directory: " + f.getPath());
					return 1;
				}
				return 0;
					
			}
			else {
				printErr("Not a directory: " + f.getPath());
				return 1;
			}
				
			
			
		}
	
		else {
			printErr("Directory does not exist: " + f.getPath() );
			return 1;
		}
	}
		
}



//
//
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
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
