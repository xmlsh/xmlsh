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
import org.xmlsh.core.ICommand;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.ThreadLocalShell;

public class SimpleCommandExpr extends CommandExpr {

  private static Logger mLogger = LogManager.getLogger();

  private CommandPrefixExpr mPrefix;
  private Word mCommand;
  private CommandSuffixExpr mSuffix;

  @Override
  public boolean isSimple() {
    return true;
  }

  /*
   * Construct a command out of a word args and redir
   * both first and words might be assignments
   */
  public SimpleCommandExpr(Word first, WordList args, IORedirectList redir) {

    mLogger.entry(first, args, redir);
    WordList cmdline = new WordList();
    cmdline.add(first);
    cmdline.addAll(args);

    for(Word w : cmdline) {
      // not to command yet
      if(mCommand == null) {
        mCommand = w;
        continue;
      }
      if(mSuffix == null)
        mSuffix = new CommandSuffixExpr();
      mSuffix.addArg(w);
    }

    if(redir != null) {
      for(IORedirect io : redir)
        mSuffix.addIO(io);
    }

    mLogger.exit();

  }

  public SimpleCommandExpr(CommandPrefixExpr prefix, Word command,
      CommandSuffixExpr suffix) {
    mLogger.entry(prefix, command, suffix);

    mPrefix = prefix;
    mCommand = command;
    mSuffix = suffix;

    mLogger.exit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.sh.core.Command#print(java.io.PrintStream)
   */
  @Override
  public void print(PrintWriter out, boolean bExec) {
    if(mPrefix != null)
      mPrefix.print(out);
    if(mCommand != null)
      mCommand.print(out);

    if(mSuffix != null) {
      out.print(" ");

      mSuffix.print(out);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.core.XEnvironment)
   */
  @Override
  public int exec(Shell shell) throws Exception {
    mLogger.entry(mCommand);

    if(mCommand == null || mCommand.isEmpty())
      return mLogger.exit(execNull(shell));

    List<XValue> cmdLine = mSuffix.toCmdLine(shell, mCommand,
        getSourceLocation());
    // Ignore empty or blank command lines
    if(cmdLine == null || cmdLine.isEmpty())
      return mLogger.exit(0);

    String cmdName = cmdLine.remove(0).toString();

    ICommand cmd = CommandFactory.getCommand(shell, cmdName,
        getSourceLocation());
    if(cmd == null) {
      logLocation(shell);
      shell.printErr(mCommand + ": not found");
      return mLogger.exit(1);
    }
    Shell saved_shell = null;
    /*
     * If there is a prefix then clone the shell, otherwise just clone the IO
     */
    boolean popIO = false;
    if(mPrefix == null) {
      shell.getEnv().saveIO();
      popIO = true;
    }
    else {
      saved_shell = shell;
      shell = shell.clone(); // must now close shell

      assert (shell != null);
      if(shell == null)
        throw mLogger
            .throwing(new UnexpectedException("Shell is null after close"));

    }

    int refcnt = 0;
    // In case we need to restore it - get it from possibly cloned shell
    Shell saved_context_shell = ThreadLocalShell.set(shell);

    try {

      if(mPrefix != null)
        mPrefix.exec(shell, getSourceLocation());

      mSuffix.exec(shell, getSourceLocation());

      // Push the current module if its different
      IModule mod = cmd.getModule();
      assert (mod != null);

      try {
        shell.pushModule(mod);
        return cmd.run(shell, cmdName, cmdLine);
      } finally {
        mod = shell.popModule();
      }

    } catch (ThrowException e) {
      throw e;// Rethrow
    }

    catch (Throwable e) {
      mLogger.catching(e);
      logLocation(shell);
      // Note: shell is the cloned shell ..
      shell.printErr("Exception running: " + cmdName + "\n" + e.toString());

      /*
       * Save exception here ????
       */

      return mLogger.exit(-1);
    }

    finally {
      mLogger.trace("exec finally: {}", this);
      ThreadLocalShell.set(saved_context_shell);

      // TODO: should I push this back into the mdoule ?
      if(!popIO) {
        mLogger.trace("Closing cloned shell {} saved {}", shell, saved_shell);
        Util.safeClose(shell);
      }
      else {
        assert (!shell.isClosed());
        if(!shell.isClosed()) {
          mLogger.trace("Restoring shell io {}", shell);
          shell.getEnv().restoreIO();
        }
      }

      mLogger.exit();
    }

  }

  private void logLocation(Shell shell) {
    shell.printLoc(mLogger, getSourceLocation());
  }

  private int execNull(Shell shell) throws Exception {

    if(mPrefix != null)
      mPrefix.exec(shell, getSourceLocation());

    if(mSuffix != null)
      mSuffix.exec(shell, getSourceLocation());

    return shell.getStatus();
  }

  @Override
  public String getName() {
    return mCommand == null ? "<command>" : mCommand.getSimpleName();
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
