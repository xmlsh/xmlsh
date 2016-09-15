package org.xmlsh.internal.commands;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import com.icl.saxon.expr.StringValue;

public class xslt1 extends XCommand {

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = new Options("i:,f:,v", SerializeOpts.getOptionDefs());
    opts.parse(args);
    args = opts.getRemainingArgs();

    String style = opts.getOptStringRequired("f");

    SerializeOpts serializeOpts = getSerializeOpts(opts);

    InputPort in = null;

    Source source = null;

    if(opts.hasOpt("i"))
      source = (in = getInput(opts.getOptValue("i"))).asSource(serializeOpts);
    else
      source = (in = getStdin()).asSource(serializeOpts);

    apply(style, source, getStdout().asOutputStream(serializeOpts),
        opts.hasOpt("v") ? args : null, serializeOpts);

    return 0;

  }

  private void apply(String style, Source source, OutputStream out,
      List<XValue> args, SerializeOpts serializeOpts)
      throws Exception {

    try {
      Templates pss = tryCache(style);
      Transformer transformer = pss.newTransformer();
      // Properties details = pss.getOutputProperties();

      if(args != null) {
        // Read pairs from args to set
        for(int i = 0; i < args.size() / 2; i++) {
          String name = args.get(i * 2).toString();
          XValue value = args.get(i * 2 + 1);

          transformer.setParameter(name, new StringValue(value.toString()));
        }
      }

      transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD,
          serializeOpts.getMethod());

      transformer.transform(source, new StreamResult(out));
    } catch (Exception err) {
      throw err;
    }

  }

  private synchronized Templates tryCache(String path)
      throws TransformerException, java.io.IOException {

    com.icl.saxon.TransformerFactoryImpl factory = new com.icl.saxon.TransformerFactoryImpl();
    return factory.newTemplates(new StreamSource(new File(path)));

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
