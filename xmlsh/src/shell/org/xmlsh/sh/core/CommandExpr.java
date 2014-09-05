/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public abstract class CommandExpr implements ICommandExpr  {
	private		SourceLocation	mLocation = null;
	private		boolean		mWait = true ;
	private String mSeparator = null ; // "\n ; & "
	private String mName = null ;


	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#isWait()
   */
	@Override
  public boolean isWait(){ return mWait ; }
	public	void	setLocation( SourceLocation loc ) { mLocation = loc ; }
	public void    setLocation(ICommandExpr c) { if( c != null && c.getLocation() != null ) mLocation =  c.getLocation() ;	}
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#getLocation()
   */
	@Override
  public	SourceLocation	getLocation() { return mLocation ; }
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#hasLocation()
   */
	@Override
  public boolean hasLocation()  { return mLocation != null && ! mLocation.isEmpty() ; }

	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#print(java.io.PrintWriter, boolean)
   */
	@Override
  public abstract void print( PrintWriter out, boolean bExec);
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#exec(org.xmlsh.sh.shell.Shell)
   */
	@Override
  public abstract int exec( Shell shell) throws Exception;

	// Is a simple command for purposes of throw-on-error
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#isSimple()
   */
	@Override
  public	abstract	boolean		isSimple() ;

	@Override
	public String	toString() {

		return toString(false);
	}


	protected CommandExpr()
	{
	}

	protected CommandExpr(String name)
	{
		setName(name) ;
	}

	public void setSeparator( String op ) {
		mSeparator = op;
		if( Util.isEqual( op , "&" ) )
			mWait = false ;
	}
	// Default name if none provided
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#getName()
   */
	@Override
  public String getName()
	{
		return Util.isBlank(mName) ? "<unnamed>" : mName ;
	}

	public String	toString(boolean bExec) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		print(w, bExec);
		w.flush();
		return sw.toString();

	}
	public void setName(String name)
	{
		mName = name;
	}

	// Helper function for simple values
	protected XValue getFirstArg( List<XValue> args  ) throws InvalidArgumentException {
		requires( ! args.isEmpty() , "Excpected arugment missing");
		return args.get(0);
	}


	protected void requires( boolean condition , String message ) throws InvalidArgumentException {
		if( ! condition )
			throw new InvalidArgumentException( getName() + ":" + message );

	}
	

  public void usage(Shell shell  , String message)
  {
    String cmdName = this.getName();
    SourceLocation sloc = getLocation();
    if( !Util.isBlank(message))
      shell.printErr(cmdName + ": " + message,sloc);
    else
      shell.printErr(cmdName + ":", sloc );
    HelpUsage helpUsage = new HelpUsage( shell );
    try {
      helpUsage.doUsage(shell.getEnv().getStdout(), cmdName);
    } catch (Exception e) {
      shell.printErr("Usage: <unknown>",sloc);
    }
  }
  public void usage(Shell shell)
  {
    usage(shell);
  }

  protected void error(Shell shell , Exception e)
  {
    shell.printErr( getName() , e);
    usage( shell ,  e.toString() );
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
