/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.properties.functions;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class fromFile extends BuiltinFunctionCommand
{


  private SerializeOpts sopts;

  public fromFile()
  {
    super("from-file");
  }

  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    Options opts = new Options( "format:" , SerializeOpts.getOptionDefs()  );
    opts.parse(args);
    sopts = shell.getSerializeOpts(opts);
    String format = opts.getOptString("format", "text");
    if( args.size() != 1 ) {
      usage( shell, "[-format text|xml|json] file");
      return null;
    }
    
    InputPort in = shell.getEnv().getInput(args.get(0));
    Properties props = new Properties();

    
    switch( format ) {
    case "text":
      try ( InputStream is = in.asInputStream(sopts) )
      {
        props.load(is );
      } ;
      break;
    case "xml" :
      try ( InputStream is = in.asInputStream(sopts) )
      {
        props.loadFromXML(is );
      } ;
      break;
    case "json" :
      loadFromJSON( props , in );
      break ;
    default : 
      usage( shell, "Unknown format: " + format );
    }
    
    return XValueProperties.fromMap( props ).asXValue();
  }

  private void loadFromJSON(Properties props, InputPort in)
  {
   return ;
  }

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

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
 * 
 */