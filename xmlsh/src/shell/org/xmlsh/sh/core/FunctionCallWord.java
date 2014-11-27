/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * A function call invocation expression
 * 
 */
public class FunctionCallWord extends Word
{
	Word	 mFunction;
	WordList	mArgs;
	

	// Return static argument cardinality
	// 0 - no args 
	// -1 variable
	// > 0 delimited
	public int getArgumentCardinality()
	{
	
	    if( mArgs.isEmpty())
	        return 0;
	    int delims = 0;
	    for( Word arg : mArgs )
	        if( arg.isDelim() )
	            delims++;
	    return delims > 0 ? delims + 1 :    -1 ;
	    
	}
	
	
	private static Logger mLogger = LogManager.getLogger();

	
	public FunctionCallWord(Word fn, WordList args)
	{
		super( fn.getFirstToken() );
		mFunction = fn;
		mArgs = args ;
		
		
	}

	
	public FunctionCallWord(Token t , String func, WordList args)
	{
		super(t);
		mFunction = new StringWord(t,func);
		mArgs = args;

	}
  
	@Override
	public void print(PrintWriter out)
	{
		mFunction.print(out);
		out.print("(");
		if(mArgs != null)
			mArgs.print(out);
		out.print(")");

	}

	@Override
	public boolean isEmpty()
	{
		return mFunction == null ;
	}

	@Override
	public String toString()
	{
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		print(pw);
		pw.flush();
		return sw.toString();
	}

	@Override
	public String getSimpleName()
	{
		return mFunction.getSimpleName();
	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, ParseResult result)
			throws IOException, CoreException
			{
       mLogger.entry();
       
       String fname = mFunction.expandString(shell, env);

       IFunctionExpr func = CommandFactory.getFunction(shell, fname );
       
		if(func == null)
			throw new InvalidArgumentException("Unknown function: " + mFunction);

		List<XValue> args = null ;
		EvalEnv argEnv = func.argumentEnv(env);

		// func( arg ,  , , , arg ) 
		// every delimiter makes sure that atlesast 1 arg was added
		// Empty args are the default XValue null
		// DO NOT:! !!Any delimiter collects previous args into a single sequence argument 
		// TODO: until we have function signatures, some functions assume the last args are varargs
		// like  option( option-defs , $@ )
		// so for the last arg in cardinality expand as usual
		
		if(mArgs != null){
		    
		    int card = getArgumentCardinality();
		    
		     // zero args
		     if( card == 0)
		         args = XValue.emptyList();
		    
		    
		    // Multi args no delims 
		    else  if( card < 0 )
		    {
		       args = new ArrayList<XValue>();
		       for (Word arg : mArgs)
		         args.addAll(  arg.expandToList(shell, argEnv));
		    } 
		      // Delimited fixed args
		    else {
		        
               @SuppressWarnings("unchecked")
               List<XValue> av[] = new List[card];
		       int i = 0;
		       for (Word arg : mArgs){
		           if( ! arg.isDelim() ){
		               av[i] = Util.appendList( av[i] , arg.expandToList(shell, argEnv));
		           } 
		           else 
		             i++;
		       }
	
		       args = new ArrayList<XValue>(card);
		       for(  i = 0 ; i < card-1 ; i++ )
		           args.add( XValue.newXValue(av[i] ) );
		       // AddAll the last option
		       
		       if( av[i] == null )
		           args.add( XValue.empytSequence());
		       else
		           args.addAll( av[i] );
		       
		    }
		    

			    
		}

		try {
			IModule module = func.getModule();
			assert( module != null );
			shell.pushModule(module);
			

			mLogger.warn("Need to also set the shell context");
			
			
		  XValue xret = func.run(shell, args);
			// ?? should check ret ?
			return EvalUtils.expandValueToResult(shell, xret, func.returnEnv(env), result);

		} 
		catch( CoreException | IOException e) {
			throw e ;
		}
		
		catch (Exception e) {
			throw new CoreException(e);
		}
		finally{

			IModule module = shell.popModule();
		    mLogger.trace("poped module: {}" , module ); 
					// TODO: should I push this back into the mdoule ?
				
			mLogger.exit();

		}
			
			
  }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
