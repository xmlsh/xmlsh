package org.xmlsh.internal.commands;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

/**
 * $Id: $
 * $Date: $
 *
 */

public class help extends XCommand {



	private boolean mNoBrowser = false;
	private boolean mXml = false ;


	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "h=help,n=nobrowse,xml" , SerializeOpts.getOptionDefs());
		opts.parse(args);

		mNoBrowser = opts.hasOpt("n");
		mXml = opts.hasOpt("xml");
		HelpUsage helpUsage = new HelpUsage( mShell );

		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());



		String name =null; 
		if( xvargs.size() != 1  ){
			name = "help";
		} else
			name = 	xvargs.get(0).toString();

		OutputPort stdout = mShell.getEnv().getStdout();




		if( name.equals("commands"))
			helpUsage.doHelpCommands(stdout,!mNoBrowser);
		else
			helpUsage.doHelp(stdout,name,mXml,!mNoBrowser);

		return 0;



	}



}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
