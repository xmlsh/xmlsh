/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionDefs;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.types.xtypes.XValueMap;
import org.xmlsh.util.Util;

public class template extends TextCommand {

  String format;
  XValueMap mVariables;
  private String mFormat;

  @Override
  protected OptionDefs getOptionDefs() {
    return super.getOptionDefs().withOptions(
        "t=template:,f=format:,V:,v,Vf:,vf:");
  }

  @Override
  protected void processStream(Reader r, PrintWriter w) throws IOException {

    mLogger.entry(r, w);
    StrSubstitutor sub = new StrSubstitutor(mVariables);
    StringBuilder sb = new StringBuilder();
    char cbuf[] = new char[1024];
    int len;
    while((len = r.read(cbuf)) >= 0) {
      sb.append(cbuf, 0, len);
    }

    boolean result = sub.replaceIn(sb);

    w.write(sb.toString());
    w.flush();
    mLogger.exit();
  }

  @Override
  protected List<XValue> parseOpts(List<XValue> args) throws IOException,
      CoreException, UnknownOption {
    args = super.parseOpts(args);
    mFormat = mOptions.getOptString("format", "text");
    mVariables = new XValueMap();

    if(mOptions.hasOpt("V"))
      mVariables.addAll(parseMap(mOptions.getOptValue("V")));
    // if( opts.hasOpt("Vf") )
    // variables.addAll( parseValueMap( mShell.getEnv().getInput(
    // opts.getOptValue("Vf") ) ) );
    if(mOptions.hasOpt("vf"))
      mVariables.addAll(parseTextMap(mShell.getEnv().getInput(
          mOptions.getOptValue("vf"))));

    for(int i = 0; i < args.size() - 1; i += 2) {
      mVariables.put(args.get(i).toString(), args.get(i + 1));
    }
    return args;

  }

  private XValueMap parseTextMap(InputPort input) throws IOException,
      CoreException {
    Properties props = new Properties();
    try (InputStream is = input.asInputStream(getSerializeOpts())) {
      props.load(is);
      return XTypeUtils.newMapFromProperties(props);
    }
  }

  /*
   * private XValueMap parseValueMap(InputPort input) { Properties props; try
   * ( InputStream is = input.asInputStream(getSerializeOpts()) ){
   * props.load(is); return XValueMap.fromInput( input ); }
   * 
   * }
   */
  private XValueMap parseMap(XValue value) throws InvalidArgumentException {
    return XTypeUtils.newMapFromValue(value);
  }

  @Override
  protected void process() throws Exception {
    super.processStream();

  }

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */
