/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.posix.commands;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

import java.io.File;
import java.util.Date;
import java.util.List;

public class touch extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "r=reference:" );
		opts.parse(args);

		args = opts.getRemainingArgs();
		String sRefFile = opts.getOptString("r", null);
		File refFile = sRefFile == null ? null : getFile(sRefFile);

		long modtime = refFile != null ? refFile.lastModified() :  (new Date()).getTime() ;

		for( XValue arg : args ){
			File file = getFile(arg);

			// create as needed
			file.createNewFile();
			file.setLastModified( modtime );


		}



		return 0;
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
