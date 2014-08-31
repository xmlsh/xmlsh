/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.core.XValuePropertyList;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;

public class Assign {
  
  
	private static final EvalEnv mListVarEnv = EvalEnv.newInstance(false, true,false, false);
	private static final EvalEnv mSingleVarEnv = EvalEnv.basicInstance();
  private static final EnumSet<XVarFlag> SEQ_VAR_FLAGS = XVariable.standardFlags( XVariable.sequenceFlags() );
  private static final EnumSet<XVarFlag> VAR_FLAGS = XVariable.standardFlags();
	private boolean	mLocal = false ;
	private	 String		mVariable;
	private String		mOp;		// "=" or "+-" 
	private Word        mInd ;      // [ index ] 
	private Word		mRValue;		// a single value a=b
	private WordList	mRValList; // a sequence constructor a=(b)
	private String   mTypeStr;  // =[ =( ={ 
	
	public Assign(boolean local  , String typeStr , String variable, String op , Word rvalue) {
		mLocal = local ;
		mTypeStr = typeStr;
		mVariable = variable;
		mOp = op ;
		mRValue = rvalue;

	}
	   
	public Assign(boolean local  ,String typeStr , String variable, Word ind , String op , Word rvalue) {
        mLocal = local ;
        mTypeStr = typeStr;
        mVariable = variable;
        mOp = op;
        mInd = ind ;
        mRValue = rvalue;
	}
	public Assign(boolean local , String typeStr,String variable, String op , WordList rvalue) {
		mLocal = local ;
    mTypeStr = typeStr;
		mVariable = variable;
		mOp = op;
		mRValList = rvalue;
	}
	public void print(PrintWriter out) {
		if( mLocal )
			out.print("local ");
		out.print(getVariable());
		if( mInd != null ) {
		    out.print("[");
		    mInd.print(out);
		    out.print("]");
		    
		}
		out.print(mOp);
		if( mRValue != null )
			mRValue.print(out);
		else
		{
			out.print("(");
			boolean bFirst = true ;
			for( Word w: mRValList ){
				if( ! bFirst )
					out.print(" ");
				w.print(out);
				bFirst = false ;

			}
			out.print(")");

		}
		out.print( " ");

	}
	public String getOp() 
	{
		return mOp ;
	}
	public String getVariable() {
		return mVariable;
	}
	public boolean isLocal () {
		return mLocal ;
	}
	public void eval(Shell shell, SourceLocation loc) throws IOException, CoreException {

		XValue value = null;

		if( mRValue == null && mRValList == null)
		    value = nullValue( mTypeStr );
		

		// Eval RHS
		else
		if( mRValue != null )
			// Single variables dont expand wildcards
			value = mRValue.expand(shell, mSingleVarEnv, loc);
		else
	    if( mRValList != null )
				// Sequences expand wildcards
				value = mRValList.expand(shell, mListVarEnv,loc);

		// Assign
		if( getOp().equals("+="))
			shell.getEnv().appendVar( getVariable(), value , getVarFlags(value),  isLocal());
		else {
		    
		    if( mInd != null ) {
		        String ind = mInd.expandString(shell, mListVarEnv, loc);
	            shell.getEnv().setIndexedVar( getVariable(), value , ind ,  getVarFlags(value), mLocal );
		    }
		    else
			     shell.getEnv().setVar( getVariable(), value , getVarFlags(value), mLocal );
		}

	}

  private XValue nullValue(String typeStr)
  {
    switch( typeStr )
    {
    case "=" :
    case "+=" :
      return XValue.nullValue();
    case "[]" :
       return new XValue( TypeFamily.XTYPE , XValueArray.emptyArray() );
    case "{}" :
      return new XValue( TypeFamily.XTYPE , XValuePropertyList.emptyPropertyList() );
    case "()" :
      return  new XValue( TypeFamily.XTYPE , XValueSequence.emptySequence());
    }
    return XValue.nullValue();
  }
  private EnumSet<XVarFlag> getVarFlags(XValue value)
  {
    if( value == null )
      return VAR_FLAGS ;
    else      
      return Util.withEnumsAdded(VAR_FLAGS, value.typeFlags());
    
    
  }
  
  
}


//
//
//Copyright (C) 2008-2014    David A. Lee.
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
