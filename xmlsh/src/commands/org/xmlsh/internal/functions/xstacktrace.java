/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.functions;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

import java.util.List;

public class xstacktrace extends BuiltinFunctionCommand {

	public xstacktrace()
	{
		super("xstacktrace");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception 
	{

		Options opts = new Options(xlocation.XLOC_OPTS,SerializeOpts.getOptionDefs());

		opts.addOptionDefs("f=function");
		opts.parse(args);

		boolean funcCalls = opts.hasOpt("f" )  && ! opts.hasOpt("depth") ;


		xlocation xloc = new xlocation();
		XValue ret = new XValue();

		Shell sh = shell ;
		while( sh != null ){

			ret = ret.append( xloc.run(sh, opts , -1 ));
			if( funcCalls && sh.hasCallStack() ) {
				for( int depth = 0 ; depth < sh.getCallStack().size() ;depth++  ) {
					XValue xv = xloc.run( sh , opts , depth );
					if( xv == null )
						shell.printErr("null stack entry at depth: " + depth );
					else
						ret = ret.append( xv );
				}

			}
			sh = sh.getParent();

		}
		return ret ;


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
