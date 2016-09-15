/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.internal.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.INamingStrategy;
import org.xmlsh.util.Util;
import org.xmlsh.util.commands.CSVFormatter;
import org.xmlsh.util.commands.CSVRecord;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/*
 * 
 * Convert XML files to an CSV file
 * 
 * Arguments
 * 
 * -header Add a header row
 * 
 * 
 */

public class xml2csv extends XCommand {

  private boolean bHeader = false;
  private String mRowXpath = "/*/*";
  private String mFieldXPath = "*/string()";
  private String mHeaderXPath = "*/node-name()";
  private boolean bAttr = false;

  private XQueryCompiler mCompiler;

  INamingStrategy mNamingStrategy;

  private static Logger mLogger = LogManager.getLogger();

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = new Options(
        "nameing=nameing-strategy:,header,attr,delim:,quote:,tab,newline:",
        SerializeOpts.getOptionDefs());
    opts.parse(args);
    setSerializeOpts(opts);

    bHeader = opts.hasOpt("header");
    bAttr = opts.hasOpt("attr");

    String delim = opts.getOptString("delim", ",");
    String quote = opts.getOptString("quote", "\"");

    // -tab overrides -delim
    if(opts.hasOpt("tab"))
      delim = "\t";
    mNamingStrategy = Util
        .getNamingStrategy(opts.getOptString("nameing-strategy", "simple"));
    OutputPort stdout = getStdout();

    try (OutputStream os = stdout.asOutputStream(getSerializeOpts());
        Writer w = new OutputStreamWriter(os,
            getSerializeOpts().getOutput_text_encoding());
        CSVFormatter mFormatter = new CSVFormatter(w, delim.charAt(0),
            quote.charAt(0))) {

      Processor processor = Shell.getProcessor();
      mCompiler = processor.newXQueryCompiler();
      InputPort in = getStdin();
      XdmNode context = in.asXdmNode(getSerializeOpts());

      // List<XValue> xvargs = opts.getRemainingArgs();
      if(bAttr) {
        mFieldXPath = "for $a in @* order by $a/name() return $a/string()";
        mHeaderXPath = "for $a in @* order by $a/name() return $a/node-name()";

      }

      XQueryExecutable expr = mCompiler.compile(mRowXpath);

      XQueryEvaluator eval = expr.load();
      if(context != null)
        eval.setContextItem(context);

      mLogger.trace("compiling header xpath: {}", mHeaderXPath);
      XQueryExecutable headerExpr = mCompiler.compile(mHeaderXPath);
      XQueryEvaluator headerEval = headerExpr.load();

      mLogger.trace("compiling field xpath: {}", mFieldXPath);
      XQueryExecutable fieldExpr = mCompiler.compile(mFieldXPath);
      XQueryEvaluator fieldEval = fieldExpr.load();

      boolean bFirst = true;
      for(XdmItem row : eval) {
        if(bFirst && bHeader) {
          writeHeader(mFormatter, row, headerEval);
          bFirst = false;
        }
        writeLine(mFormatter, row, fieldEval, false);

      }

    }

    return 0;

  }

  private void writeLine(CSVFormatter mFormatter, XdmItem row,
      XQueryEvaluator eval, boolean bHeader)
      throws SaxonApiException, IOException {
    mLogger.entry(row, eval, bHeader);
    List<String> fields = new ArrayList<String>();

    if(row != null)
      eval.setContextItem(row);

    for(XdmItem field : eval) {
      fields.add(bHeader ? mNamingStrategy.fromXmlName(fromSaxonName(field))
          : field.toString());

    }
    CSVRecord rec = new CSVRecord(fields);
    if(bHeader)
      mFormatter.writeHeader(rec);
    else
      mFormatter.writeRow(rec);

  }

  private QName fromSaxonName(XdmItem field) {
    mLogger.entry(field);
    if(field instanceof XdmNode) {
      net.sf.saxon.s9api.QName se = ((XdmNode) field).getNodeName();
      return new QName(
          se.getNamespaceURI(),
          se.getLocalName(), se.getPrefix());
    }
    else
      return new QName(field.getStringValue());
  }

  private void writeHeader(CSVFormatter mFormatter, XdmItem row,
      XQueryEvaluator eval) throws SaxonApiException, IOException {
    mLogger.entry(row, eval);
    writeLine(mFormatter, row, eval, true);

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
