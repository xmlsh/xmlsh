package org.xmlsh.internal.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XFile;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Renames one or more files based on an xpath expresion
 * 
 * 
 *
 */

public class xmove extends XCommand {

  @Override
  public int run(List<XValue> args)
      throws Exception {
    Options opts = new Options("x=xpath:,d=dir:,e=ext:,ns:+,f=force,mkdir,q");
    opts.parse(args);

    String xpath = opts.getOptStringRequired("x");
    String dir = opts.getOptString("d", ".");
    String ext = opts.getOptString("e", null);
    boolean bForce = opts.hasOpt("f");
    boolean bQuiet = opts.hasOpt("q");
    boolean bMkdir = opts.hasOpt("mkdir");

    List<XValue> xargs = opts.getRemainingArgs();

    /*
     * Precompile the xpath
     */

    Processor processor = Shell.getProcessor();
    XPathCompiler compiler = processor.newXPathCompiler();
    Namespaces ns = null;

    /*
     * Add namespaces
     */

    if(!opts.hasOpt("nons"))
      ns = getEnv().getNamespaces();
    if(opts.hasOpt("ns")) {
      Namespaces ns2 = new Namespaces();
      if(ns != null)
        ns2.putAll(ns);

      // Add custom name spaces
      for(XValue v : opts.getOptValues("ns"))
        ns2.declare(v);
      ns = ns2;
    }

    if(ns != null) {
      for(String prefix : ns.keySet()) {
        String uri = ns.get(prefix);
        compiler.declareNamespace(prefix, uri);

      }
    }

    XPathExecutable expr = compiler.compile(xpath);

    int failed = 0;
    for(XValue v : xargs) {

      XFile inFile = new XFile(getFile(v));

      String extension = ext;
      if(extension == null)
        extension = inFile.getExt();

      if(!move(inFile.getFile(), expr, dir, extension, bForce, bQuiet, bMkdir))
        failed++;

    }

    return failed;

  }

  /**
   * Do the actual rename by
   * Evaluating the xpath expression as a string
   * Optionally adding the extension
   * 
   * @throws IOException
   * @throws SaxonApiException
   */

  private boolean move(File inFile, XPathExecutable expr, String dir,
      String ext, boolean force, boolean bQuiet, boolean bMkdir)
      throws IOException, SaxonApiException {

    expr.load();

    DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
    XdmNode context = builder.build(inFile);
    XPathSelector eval = expr.load();
    eval.setContextItem(context);

    XdmItem res = eval.evaluateSingle();
    String base = res == null ? null : res.getStringValue();

    if(Util.isBlank(base)) {
      this.printErr("XPath expression evalates to null string - skipping "
          + inFile.getName());
      return false;
    }

    String toName = base + ext;
    File toFile = new File(getFile(dir), toName);
    if(!bQuiet)
      printErr(
          "Moving " + inFile.getName() + " to " + toFile.getAbsolutePath());

    if(bMkdir) {
      File parent = toFile.getParentFile();
      if(parent != null && !parent.exists())
        parent.mkdirs();

    }

    Util.moveFile(inFile, toFile, force);

    return true;

  }
};
