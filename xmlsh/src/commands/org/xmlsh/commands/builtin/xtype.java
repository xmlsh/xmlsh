/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.builtin;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellModuleURIResolver;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.util.List;

public class xtype extends BuiltinCommand {
	private boolean bFirst = true ;
	private boolean bAnyOut = false ;
	private QName mVqname;
	private XQueryEvaluator mEval;
	private OutputPort mStdout;
	private Destination mSer;

	@Override
	public int run(  List<XValue> args ) throws Exception 
	{
		Options opts = new Options("v:" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts( opts );
		Processor  processor  = Shell.getProcessor();
		String varname = opts.getOptString("v",null);

		XQueryCompiler compiler = processor.newXQueryCompiler();
		compiler.setModuleURIResolver(new ShellModuleURIResolver(mShell));

		XQueryExecutable expr = null;

		expr = compiler.compile( 
				"import module namespace functx = 'http://www.functx.com' ;\n"+ 
						"declare variable $A external;\n" + 
						"functx:sequence-type($A)"

				);



		mEval = expr.load();	
		mVqname = Util.resolveQName( "A", null );

		try {
			mStdout = mShell.getEnv().getStdout();
			mSer = mStdout.asDestination(getSerializeOpts());


			if( varname != null ) {
				XVariable xv = getShell().getEnv().getVar(varname);
				if( xv == null )
					this.printErr("Unknown variable: " + varname );
				else
					printType( xv.getValue() );
			}

			else
				for( XValue arg : opts.getRemainingArgs() ){
					printType( arg );

				}
			if( bAnyOut )
				mStdout.writeSequenceTerminator(getSerializeOpts()); // write "\n"

		} finally {
			if( mSer != null)
				mSer.close();
		}
		return 0;




	}

	void printType(XValue arg) throws InvalidArgumentException, IOException, CoreException, SaxonApiException
	{
		if( arg == null )
			throw new NullPointerException();
		if( arg.isObject() || arg.isNull() ){
			if( ! bFirst )
				mStdout.writeSequenceSeperator(getSerializeOpts()); // Thrashes variable output !
			bFirst = false ;
			if( arg.isNull() )
				Util.writeXdmValue(new XValue("null").asXdmValue(), mSer);
			else
				Util.writeXdmValue(new XValue(arg.asObject().getClass().getName()).asXdmValue(), mSer);
			bAnyOut = true ;
		}

		else {
			mEval.setExternalVariable( mVqname , arg.asXdmValue()  );


			for( XdmItem item : mEval ){
				bAnyOut = true ;
				if( ! bFirst )
					mStdout.writeSequenceSeperator(getSerializeOpts()); // Thrashes variable output !
				bFirst = false ;


				if( item instanceof XdmNode ){
					XdmNode node = (XdmNode) item ;
				}

				//processor.writeXdmValue(item, ser );
				Util.writeXdmValue(item, mSer);

			}
		}

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
