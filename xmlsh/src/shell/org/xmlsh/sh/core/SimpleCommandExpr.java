/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.xpath.ShellContext;

public class SimpleCommandExpr extends CommandExpr {


	private static Logger mLogger = LogManager.getLogger();

	private CommandPrefixExpr  mPrefix;
	private Word			mCommand;
	private CommandSuffixExpr	mSuffix;
	@Override
	public	boolean		isSimple() { return true ; }


	/*
	 * Construct a command out of a word args and redir
	 * both first and words might be assignments 
	 */
	public SimpleCommandExpr( Word first, WordList args , IORedirectList redir ) {

		WordList cmdline = new WordList();
		cmdline.add(first);
		cmdline.addAll(args);

		for( Word w : cmdline ) {
			// not to command yet 
			if( mCommand == null  ) {
				if( w instanceof StringWord ) {
					String s = w.toString();
					Assign assign = null;// parseAssign( s );
					if( assign != null ) {
						if( mPrefix == null )
							mPrefix = new CommandPrefixExpr();
						mPrefix.add(assign);
						continue ;
					}
				}
				mCommand = w ;
				continue ;
			}
			if( mSuffix == null ) 
				mSuffix = new CommandSuffixExpr();
			mSuffix.addArg(w);
		}

		if( redir != null ) {
			for( IORedirect io : redir )
				mSuffix.addIO( io );
		}

	}


	public SimpleCommandExpr(CommandPrefixExpr prefix , Word command, CommandSuffixExpr suffix )
	{

		mPrefix = prefix;
		mCommand = command;
		mSuffix = suffix ;

	}



	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintStream)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		if( mPrefix != null )
			mPrefix.print(out);
		if( mCommand != null )
			mCommand.print(out);

		if( mSuffix  != null ){
			out.print(" ");

			mSuffix.print(out);
		}

	}



	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.core.XEnvironment)
	 */
	@Override
	public int exec(Shell shell) throws Exception {



		if( mCommand == null || mCommand.isEmpty() )
			return execNull( shell );

		List<XValue>	cmdLine = mSuffix.toCmdLine(shell, mCommand , getLocation() );
		// Ignore empty or blank command lines
		if( cmdLine == null || cmdLine.isEmpty() )
			return 0;

		String cmdName = cmdLine.remove(0).toString();

		ICommand cmd = CommandFactory.getInstance().getCommand( shell , cmdName , getLocation() );

		if( cmd == null ){
			logLocation(shell);

			shell.printErr(mCommand + ": not found");
			return 1;

		}


		Shell		   saved_shell = null;
		IModule		   saved_module = null;

		/*
		 * If there is a prefix then clone the shell, otherwise just clone the IO
		 */
		if( mPrefix == null )
			shell.getEnv().saveIO();

		else {
			saved_shell = shell ;
			shell = shell.clone();

		}

		saved_module = shell.getModule();

		Shell saved_context_shell = ShellContext.set( shell );
		try {


			if( mPrefix != null )
				mPrefix.exec( shell, getLocation() );



			mSuffix.exec( shell, getLocation() );

			// Push the current module if its different
			IModule module = cmd.getModule();
			if( module != null )
				shell.setModule(module);

			return cmd.run(  shell, cmdName , cmdLine );


		} 
		catch( ThrowException e )
		{
			cmd.close();
			throw e ;// Rethrow 
		}

		catch( Exception e ){

	///		mLogger.catching( e );
			logLocation(shell);
			shell.printErr("Exception running: " +  cmdName + "\n" +  e.toString() );

			/*
			 * Save exception here ???? 
			 */

			return -1;
		}

		finally {
			ShellContext.set(saved_context_shell);
			if( ! shell.isClosed() ) {
				if( mPrefix == null )
					shell.getEnv().restoreIO();
				else
					if( saved_shell != null )
						shell.close();
				shell.setModule( saved_module );
			}
		}

	}



	private void logLocation(Shell shell) {
		shell.printLoc( mLogger , getLocation() );
	}



	private int execNull(Shell shell) throws Exception {

		if( mPrefix != null )
			mPrefix.exec( shell, getLocation() );


		if( mSuffix != null )
			mSuffix.exec( shell, getLocation() );

		return shell.getStatus();
	}

	@Override
	public String getName() {
		return mCommand == null ? "<command>" : mCommand.getSimpleName();
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
