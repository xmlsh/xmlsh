/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.posix.commands;

import java.io.File;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class mkdir extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "p=parents" );
		opts.parse(args);
		args = opts.getRemainingArgs();
		int ret = 0;


		boolean bParents = opts.hasOpt("p");
		for( XValue arg : args ){
			File f = getFile(arg);
			boolean bOk ;
			if( bParents )
				bOk = f.mkdirs();
			else
				bOk = f.mkdir();
			if( ! bOk ){
				printErr("Error creating directory: " + f.getAbsolutePath() );
				ret++;

			}

		}

		return ret;
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
