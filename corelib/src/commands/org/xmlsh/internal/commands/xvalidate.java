/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.util.ArrayList;
import java.util.List;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.DTDValidator;

public class xvalidate extends XCommand {

  @Override
  public int run(List<XValue> args)
      throws Exception {

    Options opts = new Options("xsd:,dtd:,rng:,schematron:",
        SerializeOpts.getOptionDefs());
    opts.parse(args);

    String schema = null;
    String dtd = null;
    String rng = null;
    String schematron = null;
    if(opts.hasOpt("dtd"))
      dtd = opts.getOptStringRequired("dtd");
    else if(opts.hasOpt("rng"))
      rng = opts.getOptStringRequired("rng");
    else if(opts.hasOpt("schematron"))
      schematron = opts.getOptStringRequired("schematron");

    else
      schema = opts.getOptStringRequired("xsd");

    SerializeOpts sopts = getSerializeOpts(opts);
    args = opts.getRemainingArgs();
    InputPort in = null;

    // Schematron is a special case, runs as a shell script
    if(schematron != null) {
      return run_schematron(schematron, args);
    }
    else if(rng != null) {
      return run_rng(rng, args);
    }

    if(args.size() > 0)
      in = getInput(args.get(0));
    else
      in = getStdin();

    if(schema != null) {
      return run_xsd(schema, args);
    }
    else if(dtd != null) {
      DTDValidator v = new DTDValidator(getEnv().getShell().getURL(dtd));
      v.validate(in.getSystemId(), in.asInputStream(sopts));
    }

    return 0;

  }

  private int run_xsd(String xsd, List<XValue> args) throws Exception {

    Shell shell = getEnv().getShell();
    ICommand cmd = CommandFactory.getCommand(shell, "xsdvalidate",
        getLocation());
    ArrayList<XValue> al = new ArrayList<XValue>();
    al.add(XValue.newXValue(xsd));
    al.addAll(args);

    return cmd.run(shell, "xsdvalidate", al);

  }

  private int run_schematron(String schematron, List<XValue> args)
      throws Exception {

    Shell shell = getEnv().getShell();
    ICommand cmd = CommandFactory.getCommand(shell, "schematron",
        getLocation());
    ArrayList<XValue> al = new ArrayList<XValue>();
    al.add(XValue.newXValue(schematron));
    al.addAll(args);

    return cmd.run(shell, "schematron", al);

  }

  private int run_rng(String rng, List<XValue> args) throws Exception {

    Shell shell = getEnv().getShell();
    ICommand cmd = CommandFactory.getCommand(shell, "rngvalidate",
        getLocation());
    ArrayList<XValue> al = new ArrayList<XValue>();
    al.add(XValue.newXValue(rng));
    al.addAll(args);

    return cmd.run(shell, "rngvalidate", al);

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
