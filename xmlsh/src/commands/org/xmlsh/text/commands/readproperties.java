/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.text.commands;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.sh.shell.SerializeOpts;
public class readproperties extends BuiltinCommand
{
  @Override
  public int run(List<XValue> args) throws Exception
  {
    Options opts = new Options("format:,file:,delim:,t=tree", SerializeOpts.getOptionDefs());
    opts.parse(args);
    setSerializeOpts(opts);
    String format = opts.getOptString("format", "text");
    args = opts.getRemainingArgs();
    if(args.size() != 1) {
      usage("[-format text|xml|json] file");
      return 1;
    }

    InputPort in = opts.hasOpt("file") ? getInput(opts.getOptValue("file")) : getStdin() ;
    Properties props = new Properties();
    if(args.size() < 1)
      usage("missing variable name");

    String varName = args.get(0).toString();
    mShell.getEnv().unsetVar(varName);

    switch (format) {
    case "text":
      try (InputStream is = in.asInputStream(getSerializeOpts())) {
        props.load(is);
      }
      ;
      break;
    case "xml":
      try (InputStream is = in.asInputStream(getSerializeOpts())) {
        props.loadFromXML(is);
      }
      ;
      break;
    case "json":
      loadFromJSON(props, in);
      break;
    default:
      usage("Unknown format: " + format);
    }

    XValueProperties xp = XValueProperties.fromMap(props);
    if( opts.hasOpt("tree") )
      xp = xp.expandTree(opts.getOptString("delim","."));
    XValue value = xp.asXValue();
    
    mShell.getEnv().setVar(varName, value);
    return 0;

  }

  private void loadFromJSON(Properties props, InputPort in)
  {
    return;
  }

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */