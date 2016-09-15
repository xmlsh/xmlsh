/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.xmlsh.annotations.Command;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionDef;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/**
 * Command: xconv
 * 
 * 
 * @author David A. Lee
 */

@Command("xconv")
public class xconv extends XCommand {

  private Consumer<? super String> linewriter(final String eol,
      PrintWriter out) {
    return (l) -> {
      out.print(l);
      if(eol != null)
        out.print(eol);
    };
  }

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = new Options(
        "eol-mode:,dos2unix=d2u,unix2dos=u2d,crlf=dos,lf=unix,system,java,nul",
        SerializeOpts.getOptionDefs());
    opts.parse(args);
    args = opts.getRemainingArgs();
    final String eol = getEol(opts);

    try (
        PrintWriter out = getStdout().asPrintWriter(getSerializeOpts(opts));) {

      Consumer<? super String> eolwriter = linewriter(eol, out);

      if(args.size() > 0) {
        for(XValue arg : args) {
          try (
              BufferedReader in = new BufferedReader(getShell().getEnv()
                  .getInput(arg).asReader(getSerializeOpts()));) {
            in.lines().forEach(eolwriter);
          }
        }
      }
      else {
        try (
            BufferedReader in = new BufferedReader(
                getShell().getEnv().getStdin().asReader(getSerializeOpts()));) {
          in.lines().forEach(eolwriter);
        }

      }
    }
    return 0;
  }

  private String getEol(Options opts) throws InvalidArgumentException {
    final String syseol = Util.getNewlineString();
    @SuppressWarnings("serial")
    Map<String, String> eolmap = new HashMap<String, String>() {
      {

        put("windows", "\r\n");
        put("crlf", "\r\n");
        put("dos", "\r\n");
        put("u2d", "\r\n");
        put("dos2unix", "\r\n");
        put("unix", "\n");
        put("d2u", "\n");
        put("dos2unix", "\n");
        put("lf", "\n");
        put("cr", "\r");
        put("system", syseol);
        put("java", syseol);
        put("default", syseol);
        put("nul", "");
      }
    };

    if(opts.hasOpt("eol-mode"))
      return eolmap.getOrDefault(opts.getOptStringRequired("eol-mode"), syseol);
    for(OptionValue ov : opts.getOpts()) {
      OptionDef def = ov.getOptionDef();
      if(eolmap.containsKey(def.getName()))
        return eolmap.get(def.getName());
      if(eolmap.containsKey(def.getLongname()))
        return eolmap.get(def.getLongname());
    }

    return opts.getOptString("eol", syseol);

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
