/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class mv extends XCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( "f=force" , args );
		opts.parse();
		
		
		args = opts.getRemainingArgs();
		if( args.size() < 2 ){
			usage();
			return -1;
		}
		boolean bForce = opts.hasOpt("f");
		XValue last = args.remove(args.size()-1);
		File target = getFile( last );
		
		if( args.size() > 2 || target.isDirectory() ){

			if( ! target.isDirectory() )
				throw new InvalidArgumentException("Target is not a directory: " + target.getName() );
			move( args , target , bForce );
			
		} else {
			
			move( getFile(args.get(0)) , target , bForce);
		}
				
		return 0;
	}

	private void copy(File src, File dest, boolean force) throws IOException {

		// Try copy 
		InputStream in = null;
		OutputStream out = null;
		try {
		
			in = new FileInputStream(src);
			
			// Try deleting dest if we have to
			if( force && dest.exists() && ! dest.canWrite() )
				dest.delete();
			
			out = new FileOutputStream(dest);
			Util.copyStream( in , out );
		} finally {
			if( in != null ) in.close();
			if( out != null ) out.close();
		}
	}

	private void move(List<XValue> files, File target, boolean force) throws IOException {
		for( XValue f : files ){
			File src = getFile(f);
			File dest = new File( target , src.getName() );
			move( src , dest , force );
			
			
			
		}
	}

	private void usage() {
		printErr("usage: cp [-f] file1 [file2 ....] dest");
		
	}

	private void move(File src, File dest, boolean force) throws IOException {
		if( dest.exists() && force)
			dest.delete();
		
		// Simple rename
		if( src.renameTo(dest))
			return ;
		
		copy(src,dest,force);
		src.delete();
		
	}


}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
