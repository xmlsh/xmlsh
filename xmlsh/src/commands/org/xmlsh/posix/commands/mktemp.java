/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.posix.commands;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.FileUtils;

import java.io.File;
import java.util.List;

public class mktemp extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = new Options( "d=directory,s=suffix:,tempdir:,p=prefix:" , SerializeOpts.getOptionDefs() );
		opts.parse(args);

		args = opts.getRemainingArgs();

		if( args.size() != 0 ){
			usage();
			return -1;
		}


		File tmp = 
				File.createTempFile(opts.getOptString("p", "xmlsh") , opts.getOptString("s", null) , 
						opts.hasOpt("tempdir") ? this.getFile( opts.getOptValue("tempdir") ) : null );

		if( opts.hasOpt("d")){
			tmp.delete();
			tmp.mkdirs();

		}
		OutputPort out = this.getStdout();
		out.asPrintStream(getSerializeOpts(opts)).println( FileUtils.convertPath(tmp.getAbsolutePath(),false) );
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
