/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class tee extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(	SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		
		// List of outputs to tee to
		List<OutputStream>		writers = new ArrayList<OutputStream>();
		InputPort stdin = getStdin();
		
		
		SerializeOpts sopts = getSerializeOpts(opts);
		
		try (
				InputStream is = new BufferedInputStream(stdin.asInputStream(sopts));
		){
				
			OutputPort stdout = getStdout();
			writers.add(new BufferedOutputStream(stdout.asOutputStream(sopts)));
			
			for( XValue arg : args ){
				OutputPort output = getEnv().getOutput(arg, false);
				writers.add( new BufferedOutputStream(output.asOutputStream(sopts)));
			}
			
			int c ;

			while((c=is.read()) > 0 ){
				for( OutputStream out : writers )
					out.write(c);
			}
			
			for( OutputStream out : writers )
				out.close();
			
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
