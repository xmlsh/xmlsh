/**
 * $Id: xcat.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xgunzip extends XCommand {
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "f=file:,o=out:" ,  SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		XValue zipfile = opts.getOptValue("f");
		
		args = opts.getRemainingArgs();
		
		SerializeOpts serializeOpts = getSerializeOpts(opts);

		InputPort iport = (zipfile == null ? getStdin() : getInput(zipfile));
		InputStream is = iport.asInputStream(serializeOpts); 
		
		GZIPInputStream zis = new GZIPInputStream(is);
		
		XValue xout = opts.getOptValue("out");
		
		OutputPort oport = this.getOutput(xout, false);
		
		
		
		
		try {
		
		int ret = 0;
		ret = gunzip( zis , oport.asOutputStream(serializeOpts) , args );
		
	
		zis.close();
		
		return ret;
		
		} finally {
			zis.close();
			is.close();
			iport.release();
			oport.release();
		}
		


	}

	private int gunzip(GZIPInputStream zis, OutputStream out , List<XValue> args) throws IOException {
	
	
	 byte[] buf = new byte[1024];
	    int len;
	    while ((len = zis.read(buf)) > 0)
	        out.write(buf, 0, len);
	 
	    zis.close();
	    out.close();
	 

		return 0;
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
