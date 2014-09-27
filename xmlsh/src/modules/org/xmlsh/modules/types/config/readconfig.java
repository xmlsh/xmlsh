/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.modules.types.config;

import java.util.List;
import java.util.Properties;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XConfiguration;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.text.TextConfigParser;
public class readconfig extends XCommand
{
  private final class IniConfigParser extends TextConfigParser
  {
    @Override
    protected XValue parseValue(String currentSection, String name, String value) throws InvalidArgumentException
    {
      return XValue.newInstance(value);
    }
  }

  @Override
  public int run(List<XValue> args) throws Exception
  {
    Options opts = new Options("format:,file:", SerializeOpts.getOptionDefs());
    opts.parse(args);
    setSerializeOpts(opts);
    String format = opts.getOptString("format", "text");
    args = opts.getRemainingArgs();
    if(args.size() != 1) {
      usage("[-format ini|xml|json] [-file file] variable");
      return 1;
    }

    InputPort in = opts.hasOpt("file") ? getInput(opts.getOptValue("file")) : getStdin() ;


    XConfiguration config = null ;
    
    
    String varName = args.get(0).toString();
    mShell.getEnv().unsetVar(varName);

    switch (format) {
    case "text" :
    case "ini":
      
      TextConfigParser parser = new IniConfigParser();
      config = parser.loadConfig( in.asReader(getSerializeOpts()));
      
      break;
    case "xml":
    case "json":
    default:
      usage("Unsupported format: " + format);
    }
    
    mShell.getEnv().setVar(varName, config.asXValue() );
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