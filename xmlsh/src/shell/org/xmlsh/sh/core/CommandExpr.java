/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
/*
 * An expression that evaluates as running a 'command' by running exec()
 */
public abstract class CommandExpr extends AbstractExpr implements ICommandExpr  {
	private		SourceLocation	mLocation = null;
	private		boolean		mWait = true ;
	private String mSeparator = null ; // "\n ; & "

	protected CommandExpr(){
	  super();
	}
	protected CommandExpr(String name)
  {
    super(name);
  }
  /* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#isWait()
   */
	@Override
  public boolean isWait(){ return mWait ; }
	public	void	setLocation( SourceLocation loc ) { mLocation = loc ; }
	public void    setLocation(ICommandExpr c) { if( c != null && c.hasLocation() ) mLocation =  c.getSourceLocation() ;	}
	/* (non-Javadoc)
   * @see org.xmlsh.sh.core.ICommandExpr#getLocation()
   */
	
	@Override
  public	SourceLocation	getSourceLocation() { return mLocation ; }
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



	public void setSeparator( String op ) {
		mSeparator = op;
		if( Util.isEqual( op , "&" ) )
			mWait = false ;
	}
	@Override
	public String	toString(boolean bExec) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		print(w, bExec);
		w.flush();
		return sw.toString();

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
