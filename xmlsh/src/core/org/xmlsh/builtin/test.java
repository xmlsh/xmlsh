/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * The "test" command implements a subset of the unix test (1) command.
 * aka "["
 * 
 */


/***************************************************************

DESCRIPTION
Exit with the status determined by EXPRESSION.


An omitted EXPRESSION defaults to false.  Otherwise, EXPRESSION is true
or false and sets exit status.  It is one of:

( EXPRESSION )
   EXPRESSION is true

! EXPRESSION
   EXPRESSION is false

EXPRESSION1 -a EXPRESSION2
   both EXPRESSION1 and EXPRESSION2 are true

EXPRESSION1 -o EXPRESSION2
   either EXPRESSION1 or EXPRESSION2 is true

-n STRING
   if a string the length of STRING is nonzero or
   if xml then the sequence is not the null sequence


STRING equivalent to -n STRING

-z STRING
   the length of STRING is zero
   
-S value
	true if the value/argument is a string (not an xml type)
	
-X value
	true if the value/argument is an xml type
	

STRING1 = STRING2
   the strings are equal

STRING1 != STRING2
   the strings are not equal

INTEGER1 -eq INTEGER2
   INTEGER1 is equal to INTEGER2

INTEGER1 -ge INTEGER2
   INTEGER1 is greater than or equal to INTEGER2

INTEGER1 -gt INTEGER2
   INTEGER1 is greater than INTEGER2

INTEGER1 -le INTEGER2
   INTEGER1 is less than or equal to INTEGER2

INTEGER1 -lt INTEGER2
   INTEGER1 is less than INTEGER2

INTEGER1 -ne INTEGER2
   INTEGER1 is not equal to INTEGER2

FILE1 -ef FILE2
   FILE1 and FILE2 have the same cananocal path

FILE1 -nt FILE2
   FILE1 is newer (modification date) than FILE2

FILE1 -ot FILE2
   FILE1 is older than FILE2


-d FILE
   FILE exists and is a directory

-e FILE
   FILE exists

-f FILE
   FILE exists and is a regular file

-r FILE
   FILE exists and read permission is granted

-s FILE
   FILE exists and has a size greater than zero

-w FILE
   FILE exists and write permission is granted

-x FILE
   FILE exists and execute (or search) permission is granted

Beware	that  parentheses  need  to be escaped (e.g., by back-
slashes) for shells.  INTEGER may also be -l STRING, which evaluates to
the length of STRING.


******************************************************/




public class test extends BuiltinCommand {
	

	private		Shell 	mShell;
	
	@SuppressWarnings("serial")
	private static class Error extends Exception
	{

		public Error(final String message) {
			super(message);
		}
		
	};

	
	private File 	getFile( XValue value ) throws Error
	{
		try {
			return mShell.getFile(value).getCanonicalFile();
		} catch (IOException e) {
			throw new Error("IOException resolving file: " + value );
		}
	}
	
	
	private 	boolean		eval( List<XValue> av) throws Error, UnexpectedException
	{
		if( av.size() == 0 )
			return false;
		
		XValue av1 = av.remove(0);

		if( av.size() == 0 || av.get(0).equals(")") )
			return evalUnary("-n" , av1);
		
		if( av1.isString() ){


			String a1 = av1.toString();
			
			if( a1.equals("!"))
				return ! eval( av );
			
			if( a1.equals("(")){
				boolean ret = eval(av);
				if( av.size() < 1 || !av.remove(0).equals(")")){
					throw new Error("mismatched (");
	
				}
				return ret;
			}
			else
			if( a1.startsWith("-") && ! Util.isInt(a1, true)){
				if( av.size() < 1 ){
					throw new Error("expected arg after " + a1);
	
				}
				return evalUnary( a1 , av.remove(0));
			}
			
			else
			if( av.size() == 1 ){
				throw new Error("unexpected arg: " + av.remove(0));
			
			}
		}
		
		XValue op  = av.remove(0);
		if( op.isString() )
			return evalBinary( av1 ,  op.toString() , av.remove(0 ) );
		else
				throw new Error("Unexpected xml value operator");
			

		
	}
	
	
	
	
	
	
	private boolean evalBinary(XValue av1, String op, XValue value) throws Error {
		if( op.equals("="))
			return av1.equals(value);
		else
		if( op.equals("!="))
			return !av1.equals(value);
		else
		if( op.equals("-eq"))
			return compareInt( av1 , value ) == 0;
		else
		if( op.equals("-ne"))
			return compareInt(av1,value) != 0 ;
		else
		if( op.equals("-gt"))
			return compareInt(av1,value) > 0 ;
			else
		if( op.equals("-ge"))
			return compareInt( av1,value) >= 0;
			else
		if( op.equals("-lt"))
			return compareInt(av1,value) < 0;
		else
		if( op.equals("-le"))
			return compareInt(av1,value) <= 0 ;
		else
		if( op.equals("-ef"))
			return getFile(av1).compareTo(getFile(value)) == 0;
		else
		if( op.equals("-nt"))
			return getFile(av1).lastModified() >
				    getFile(value).lastModified() ;
		else
		if( op.equals("-ot" ))
			return getFile(av1).lastModified() <
		   		getFile(value).lastModified() ;		
					   
		
		else
			throw new Error("Invalid binary operator " + op);
	
	

	}

	private int compareInt(XValue a1, XValue a2) throws Error {
		
		if(!( a1.isAtomic() && a2.isAtomic() ))
			throw new Error("args must be atomic expressions");
		
		String s1 = a1.toString();
		String s2 = a2.toString();
		
		if( ! Util.isInt(s1,true) || ! Util.isInt(s2,true) ){
			throw new Error("Invalid integer expression");
			
		}
		return Util.parseInt(s1, 0) -
				Util.parseInt(s2, 0);
		
 
	}

	private boolean evalUnary(String op, XValue value) throws Error, UnexpectedException {
		
		
		/* try type tests first */
		
		if( op.equals("-X"))
			return value.isXExpr();
		else
		if(op.equals("-S"))
			return value.isString();
	

			if( op.equals("-n")){
				return value.toBoolean();
			
			}	
			else
			if( op.equals("-z")){
				// -z is opposite of -n 
				// returns true if null, zero length string or false boolean xexpr

				return ! value.toBoolean();
			}
			else
			if( op.equals("-d") )
				return getFile( value ).isDirectory();
			else
			if( op.equals("-e"))
				return getFile(value).exists();
			else
			if( op.equals("-f"))
				return getFile(value).isFile();
			else
			if( op.equals("-r"))
				return getFile(value).canRead();
			else
			if( op.equals("-s"))
				return getFile(value).length() > 0;
			else
			if( op.equals("-w") )
				return getFile(value).canWrite();
			else
			if( op.equals("-x"))
				return getFile(value).canExecute();
			
			
			else {
				throw new Error("unknown test " + op);
	
			}
		
		
	}
	
	
	public int run( Shell shell, String cmd,  List<XValue> args ) throws Exception {
			
		mShell = shell;  // Used for file resolution
		
		List<XValue> av = args;

		
		if( cmd.equals("[") ){
			if( av.size() == 0 || !av.remove(av.size()-1).equals("]")){
				shell.printErr("Unbalanced [");
				return 1;
	
			}
		}
		
		
		

		
		
		boolean ret = false ;
		boolean bFirst = true ;
		try {
			while( av.size() > 0 ){
				String op = null;
				XValue a1 = av.get(0);
				if( ! bFirst &&  a1.isString() && ( a1.equals("-a") || a1.equals("-o"))){
					op = a1.toString();
					av.remove(0);
				}

				
				boolean r  = eval(av);
				if( op != null ){
					if( op.equals("-a"))	
						ret = ret && r;
					else
					if( op.equals("-o"))
						ret = ret || r ;
					else
						throw new Error("Invalid op " + op );
					
				}

				else
					ret = r;
					
				bFirst = false ;
				
			}
			
			
		} catch( Error e ){
			
			shell.printErr( cmd + ":"  + e.getMessage());
			return 1;
		}
		
		
		
		
		return Shell.fromBool(ret);
			
				
	}



}

//
//
//Copyright (C) 2008, David A. Lee.
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

