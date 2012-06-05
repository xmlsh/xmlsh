/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/**
 * Command: more
 * 
 * 
 * @author David A. Lee
 */



public class more extends XCommand {

	int mLines = 23;
	SerializeOpts	mSerial;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		Options opts = new Options( "l=lines:" , SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		mSerial = this.getSerializeOpts(opts);
		
		
		XValue vlines = getEnv().getVarValue("LINES");
		if( vlines != null )
			mLines = (int) vlines.toLong();
		
		mLines = opts.getOptInt("l", mLines);
		
		
		
		OutputStream 	stdout = getStdout().asOutputStream(mSerial);
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
				boolean bQuit = page(in, stdout);
				in.close();
				// ip.close();
				if( bQuit )
					break;
			}
		} else {

			page( getStdin().asInputStream(mSerial) , stdout );
		}
		
		return 0;
	}

	// pagenate, return true on EOF false on quit
	private boolean page(InputStream in, OutputStream stdout ) throws IOException {
		
		
		
		Console con = System.console();
		if( con == null ){
			Util.copyStream( in , stdout );
			return false;
		}
		
		int lines = mLines;
		do {
			for( int i =0 ; i < lines ; i++ ){
				String l = Util.readLine(in,mSerial.getInputTextEncoding());
				if( l == null )
					return false;
				stdout.write(l.getBytes(mSerial.getInputTextEncoding()));
				stdout.write( '\n');
				
			}
			
			//if( r.read() == 'q')
			//	return ;
			//InputStream is = new FileInputStream(FileDescriptor.in);
			//is.read();
			//is.close();
			
			char[] ret = con.readPassword("---More---");
			if( ret == null )
				return false; // EOF
			if( ret.length == 0 )
				continue;
		
			if( ret[0] == 'q' || ret[0] == 'Q')
				return true ;
			
		
		} while(true);
		
	}

}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
