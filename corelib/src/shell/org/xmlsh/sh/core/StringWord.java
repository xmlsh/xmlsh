/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression
 * 
 */
public class StringWord extends Word {
  private String mString; // String value

  public StringWord(Token t) {
    this(t, t.toString());
  }

  public StringWord(Token t, String s) {
    super(t);
    mString = s;
  }

  @Override
  public void print(PrintWriter out) {
    out.print(mString);
  }

  @Override
  public boolean isEmpty() {
    return mString.isEmpty();
  }

  @Override
  public String toString() {
    return mString;
  }

  @Override
  public String getSimpleName() {
    return isEmpty() ? "<null>" : mString;
  }

  @Override
  protected ParseResult expandToResult(Shell shell, EvalEnv env,
      ParseResult result) throws IOException, CoreException {
    return EvalUtils.expandStringToResult(shell, mString, env, result);

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
