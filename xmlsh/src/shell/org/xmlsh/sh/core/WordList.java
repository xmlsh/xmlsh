/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.Shell;

@SuppressWarnings("serial")
public class WordList extends ArrayList<Word>
{

 
  public void print(PrintWriter out)
  {
    for (Word s : this) {
      s.print(out);
      out.print(" ");
    }
  }

  @Override
  public boolean add(Word e)
  {
    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends Word> c)
  {
    return super.addAll(c);
  }

  public XValue expand(Shell shell, EvalEnv env, SourceLocation loc) throws IOException, CoreException
  {
    if(size() == 0)
      return XValue.newXValue(XValueSequence.emptySequence());
    if(size() == 1)
      return get(0).expand(shell, env);

    List<XValue> list = new ArrayList<XValue>(size());

    for (Word w : this) {
      XValue v = w.expand(shell, env);
      if((v == null || v.isNull()) && env.omitNulls())
        continue;

      list.add(v);

    }
    return XValue.newXValue(list);

  }

  public Token getFirstToken()
  {
    if(isEmpty())
      return null;

    return get(0).getFirstToken();
  }

  /*
   * Split a word list by expanding any JoinedWordList words delimited by delim, and combining free
   * 
   * public WordList splitDelim(String delim ) {
   * for( Word w : this ) {
   * if( w.isDelimiter( delim ))
   * 
   * 
   * }
   * 
   * 
   * 
   * }
   */
}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
