/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.builtin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
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
   the length of STRING is nonzero

STRING equivalent to -n STRING

-z STRING
   the length of STRING is zero

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

	
	private File 	getFile( String str ) throws Error
	{
		try {
			return mShell.getFile(str).getCanonicalFile();
		} catch (IOException e) {
			throw new Error("IOException resolving file: " + str );
		}
	}
	
	
	private 	boolean		eval( List<String> args) throws Error
	{
		if( args.size() == 0 )
			return false;
		
		String a1 = args.remove(0);
		if( a1.equals("!"))
			return ! eval( args );
		else
		if( a1.equals("(")){
			boolean ret = eval(args);
			if( args.size() < 1 || !args.remove(0).equals(")")){
				throw new Error("mismatched (");

			}
			return ret;
		}
		else
		if( a1.startsWith("-") && ! Util.isInt(a1, true)){
			if( args.size() < 1 ){
				throw new Error("expected arg after " + a1);

			}
			return evalUnary( a1 , args.remove(0));
		}
		else
		if( args.size() == 0 || args.get(0).equals(")") )
			return evalUnary("-n" , a1);
		else
		if( args.size() == 1 ){
			throw new Error("unexpected arg: " + args.remove(0));
		
		}
		else {
			String op = args.remove(0);
			

			return evalBinary( a1 ,  op , args.remove(0 ) );
		}	
		
		
	}
	
	
	
	
	
	
	
	private boolean evalBinary(String a1, String op, String a2) throws Error {
		if( op.equals("="))
			return a1.equals(a2);
		else
		if( op.equals("!="))
			return !a1.equals(a2);
		else
		if( op.equals("-eq"))
			return compareInt( a1 , a2 ) == 0;
		else
		if( op.equals("-ne"))
			return compareInt(a1,a2) != 0 ;
		else
		if( op.equals("-gt"))
			return compareInt(a1,a2) > 0 ;
			else
		if( op.equals("-ge"))
			return compareInt( a1,a2) >= 0;
			else
		if( op.equals("-lt"))
			return compareInt(a1,a2) < 0;
		else
		if( op.equals("-le"))
			return compareInt(a1,a2) <= 0 ;
		else
		if( op.equals("-ef"))
			return getFile(a1).compareTo(getFile(a2)) == 0;
		else
		if( op.equals("-nt"))
			return getFile(a1).lastModified() >
				    getFile(a2).lastModified() ;
		else
		if( op.equals("-ot" ))
			return getFile(a1).lastModified() <
		   		getFile(a2).lastModified() ;		
					   
		
		else
			throw new Error("Invalid binary operator " + op);
	
	

	}

	private int compareInt(String a1, String a2) throws Error {
		if( ! Util.isInt(a1,true) || ! Util.isInt(a2,true) ){
			throw new Error("Invalid integer expression");
			
		}
		return Util.parseInt(a1, 0) -
				Util.parseInt(a2, 0);
		
 
	}

	private boolean evalUnary(String op, String arg) throws Error {
		if( op.equals("-n"))
			return arg.length() > 0 ;
			else
		if( op.equals("-z"))
			return arg.length() == 0;
		
		else
		if( op.equals("-d") )
			return getFile( arg ).isDirectory();
		else
		if( op.equals("-e"))
			return getFile(arg).exists();
		else
		if( op.equals("-f"))
			return getFile(arg).isFile();
		else
		if( op.equals("-r"))
			return getFile(arg).canRead();
		else
		if( op.equals("-s"))
			return getFile(arg).length() > 0;
		else
		if( op.equals("-w") )
			return getFile(arg).canWrite();
		else
		if( op.equals("-x"))
			return getFile(arg).canExecute();
		
		
		else {
			throw new Error("unknown test " + op);

		}
		
	}
	
	
	public int run( Shell shell, String cmd,  List<XValue> args ) throws Exception {
			
		mShell = shell;  // Used for file resolution
		
		List<XValue> av = args;
		List<String> a = Util.toStringList(av);
		
		if( cmd.equals("[") ){
			if( a.size() == 0 || !a.remove(a.size()-1).equals("]")){
				shell.printErr("Unbalanced [");
				return 1;
	
			}
		}
		
		
		

		
		
		boolean ret = false ;
		boolean bFirst = true ;
		try {
			while( a.size() > 0 ){
				String op = null;
				if( ! bFirst && ( a.get(0).equals("-a")|| a.get(0).equals("-o")))
					op = a.remove(0);

				
				boolean r  = eval(a);
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

