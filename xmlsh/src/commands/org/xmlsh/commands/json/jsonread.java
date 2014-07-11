/**
 * $Id: jsonread.java 698 2012-06-05 23:17:15Z daldei $
 * $Date: 2012-06-05 19:17:15 -0400 (Tue, 05 Jun 2012) $
 *
 */

package org.xmlsh.commands.json;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JsonUtils;
import org.xmlsh.util.Util;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class jsonread extends BuiltinCommand {

	/*
	 *  Read JSON input and parse it as a JSON object
	 *  
	 */


	public int run( List<XValue> args ) throws Exception {

		
		Options opts = new Options( "+jsonp,p=port:" ,  SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		String port = opts.getOptString("p", null);
		boolean jsonp = opts.getOptFlag("jsonp",false);
		
		args = opts.getRemainingArgs();
		
		if( args.size() != 1 )
			throw new InvalidArgumentException("requires 1 argument");
		
		mShell.getEnv().unsetVar(args.get(0).toString());

		setSerializeOpts(getSerializeOpts());
		SerializeOpts inputOpts = getSerializeOpts().clone();
		
		
		InputPort stdin = mShell.getEnv().getStdin();
		
		InputStream is = null ;
		JsonNode node = null ;
		try {
			is = stdin.asInputStream( inputOpts );
			
			if( jsonp ) {
				String jsonpFunc = Util.skipToByte(is, '(');
				if( jsonpFunc == null ) {
					mShell.printErr("No JSONP prefix found");
					return 1;
				}
			}
			
			node = JsonUtils.toJsonNode(is);
			mShell.getEnv().setVar(args.get(0).toString(), new XValue(node) ,false);

		} finally {
			Util.safeClose(is);
			Util.safeRelease(stdin);
		}


		return node == null ? 1 : 0 ;
	}
}


//Copyright (C) 2008-2014    David A. Lee.

//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 

//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.

//The Original Code is: all this file.

//The Initial Developer of the Original Code is David A. Lee

//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.

//Contributor(s): none.

