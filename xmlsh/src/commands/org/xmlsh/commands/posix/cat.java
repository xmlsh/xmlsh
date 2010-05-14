/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

/**
 * Command: Cat
 * 
 * 
 * @author David A. Lee
 */



public class cat extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		/* Options opts = new Options( "" , args );
		 * opts.parse();
		 * args = opts.getRemainingArgs();
		 */ 
		OutputStream 	stdout = getStdout().asOutputStream(getSerializeOpts());
		if( args.size() > 0 ){
			for( XValue arg : args ){
				
				//InputStream in = getInputStream(arg);
				File inf = getFile(arg);
				//InputPort ip = this.getInput(arg);
				// InputStream in = ip.asInputStream(getSerializeOpts());
				if( ! inf.exists() ){
					this.printErr("File not found: " + arg.toString());
					continue;
				}
				if( ! inf.canRead() ){
					this.printErr("File not readable: " + arg.toString());
					continue;
				}
				 InputStream  in= new FileInputStream( inf );
				Util.copyStream( in , stdout );
				in.close();
				// ip.close();
			}
		}
		else
			Util.copyStream( getStdin().asInputStream(getSerializeOpts()) , stdout );
		
		return 0;
	}

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
