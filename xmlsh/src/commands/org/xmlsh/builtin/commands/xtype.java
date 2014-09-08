/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.builtin.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;

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
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

public class xtype extends BuiltinCommand {
	private QName mVqname;
	private XQueryEvaluator mEval;

	@Override
	public int run(  List<XValue> args ) throws Exception 
	{
		Options opts = new Options("v:,s=simple,j=java" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts( opts );
		Processor  processor  = Shell.getProcessor();
		String varname = opts.getOptString("v",null);
		boolean bSimple = opts.hasOpt("s");
		boolean bJava = opts.hasOpt("j");

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

		OutputPort mOut = mShell.getEnv().getStdout();
    try ( PrintWriter w = mOut.asPrintWriter(getSerializeOpts()) )

		{

			if( varname != null ) {
				XVariable xv = getShell().getEnv().getVar(varname);
				if( xv == null )
					this.printErr("Unknown variable: " + varname );
				else
					printType( w,  xv.getValue() , bSimple , bJava );
			}

			else
				for( XValue arg : opts.getRemainingArgs() ){
					printType( w,  arg , bSimple, bJava );

				}

		} 
		return 0;


	}

	void printType(PrintWriter w, XValue arg, boolean bSimple, boolean bJava) throws InvalidArgumentException, IOException, CoreException, SaxonApiException
	{
	  
		if( arg == null )
			throw new NullPointerException();
		
		if( bJava )
		  w.println( arg.javaTypeName() );
		else
		
//		if(  bSimple ||  arg.canConvert( XdmValue.class ) < 0 ){
		  
   if(  bSimple ||  ! arg.isTypeFamily( TypeFamily.XDM ) ) {
			String type = 
			    bSimple ? 
			        arg.typeFamilyInstance().simpleTypeName( arg.asObject() ) :
			          arg.typeFamilyInstance().typeName( arg.asObject() );   
			     
				w.println( type );
			
		}

		else {
			mEval.setExternalVariable( mVqname , arg.toXdmValue()  );

			for( XdmItem item : mEval ){
				w.println( item.getStringValue() );

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
