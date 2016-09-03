package org.xmlsh.builtin.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellOpts;
import org.xmlsh.types.xtypes.XValueProperties;
import org.xmlsh.types.xtypes.XValueProperty;
import org.xmlsh.util.Util;
import com.jayway.jsonpath.internal.Utils;

public class xshopt extends BuiltinCommand {

  static final String sDocRoot = "options";

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = new Options("+s,+u", SerializeOpts.getOptionDefs());
    opts.parse(args);
    setSerializeOpts(opts);
    if(opts.hasOpt("s"))
      setOpts(Util.toStringList(opts.getRemainingArgs()), true);
    else if(opts.hasOpt("u"))
      setOpts(Util.toStringList(opts.getRemainingArgs()), false);
    else
      printOpts(Util.toStringList(opts.getRemainingArgs()));

    return 0;

  }

  private void setOpts(List<String> remainingArgs, boolean flag) {
    remainingArgs.forEach(name -> getShell().setOption(name, flag));

  }

  private void printOpts(List<String> list)
      throws XMLStreamException, IOException, CoreException, SaxonApiException {
    OutputPort stdout = getStdout();
    XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());

    try {
      writer.writeStartDocument();
      writer.writeStartElement(sDocRoot);

      XValueProperties props = mShell.getOpts().getOptionsAsProperties();
      writeOptions(writer, props, list);

      writer.writeEndElement();
      writer.writeEndDocument();

    } finally {
      writer.close();
      stdout.writeSequenceTerminator(getSerializeOpts());
    }
  }

  private void writeOptions(XMLStreamWriter writer, XValueProperties props,
      List<String> list)
      throws XMLStreamException, InvalidArgumentException,
      UnexpectedException {
    for(XValueProperty prop : props.asPropertyList()) {
      writer.writeStartElement("option");
      writer.writeAttribute("name", prop.getKey());
      XValue value = prop.getValue();
      if(!value.isNull()) {
        if(value.isInstanceOf(XValueProperties.class))
          writeOptions(writer, value.asInstanceOf(XValueProperties.class),
              list);
        else

        if(value.isInstanceOf(Boolean.class))
          writer.writeCharacters(value.toBoolean() ? "on" : "off");
        else
          writer.writeCharacters(value.toString());
      }
      writer.writeEndElement();
    }
  }

}
