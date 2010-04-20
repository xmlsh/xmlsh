/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Base64;
import org.xmlsh.util.Util;

/**
 * Command: Cat
 * 
 * 
 * @author David A. Lee
 */



public class base64 extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		

		Options opts = new Options("w=wrap,d=decode", SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		boolean bDecode = opts.hasOpt("d");
		boolean bWrap   = opts.hasOpt("w");
		
		InputPort iport = (args.isEmpty() ? getStdin() : getInput(args.get(0)));
		InputStream is = iport.asInputStream(getSerializeOpts()); 
		OutputStream os = getStdout().asOutputStream();
		
		/*
		 * Need to buffer the streams because the B64 code does 1 byte IO 
		 */
		is = new BufferedInputStream(is);
		os = new BufferedOutputStream(os);
		
		try {
			if( bDecode )
				decode( is , os);
			else
				encode( is , os , bWrap );
		} finally {
			is.close(); // Unnecessary 
			os.close(); // Unnecessary 
			iport.release(); // Needed else file handle gets left open

		}
				
		return 0;
	}

	private void encode(InputStream is, OutputStream os, boolean wrap ) throws IOException {
		Base64.OutputStream b64 = new Base64.OutputStream( os , Base64.ENCODE | (wrap ? Base64.DO_BREAK_LINES : 0 ) ); 
		Util.copyStream(is , b64);
		b64.close();
	}

	private void decode(InputStream is, OutputStream os) throws IOException {
		Base64.InputStream b64 = new Base64.InputStream(is, Base64.DECODE );
		Util.copyStream(b64, os);
		b64.close();
		
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
