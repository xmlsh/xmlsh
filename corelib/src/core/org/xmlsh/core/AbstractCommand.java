/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import javax.xml.transform.Source;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.io.IInputPort;
import org.xmlsh.core.io.IOutputPort;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.Module;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

public abstract class AbstractCommand implements ICommand {

  @Override
  public void print(PrintWriter w, boolean bExec) {
    w.print(getName());
  }

  protected Logger mLogger = org.apache.logging.log4j.LogManager
      .getLogger(this.getClass());

  protected Shell mShell;
  protected XEnvironment mEnvironment;
  protected SourceLocation mLocation;
  private SerializeOpts mSerializeOpts = null;
  protected IModule mModule;

  @Override
  protected void finalize() {
    // Clear refs
    mShell = null;
    mEnvironment = null;
    mLocation = null;
    mSerializeOpts = null;
    mModule = null;

  }

  @Override
  public final IModule getModule() {
    mLogger.entry();

    assert (mModule != null);
    return mModule;
  }

  public void setModule(Module module) throws IOException {
    mLogger.entry(module);
    mModule = module;
  }

  public AbstractCommand(IModule module) {
    mLogger.entry(module);
    mModule = module;
    mLogger.exit();
  }

  public abstract String getName();

  protected SerializeOpts getSerializeOpts(Options opts)
      throws InvalidArgumentException {
    return mShell.getSerializeOpts(opts);
  }

  protected XEnvironment getEnv() {
    return mEnvironment;
  }

  protected InputPort getStdin() throws IOException {
    return mEnvironment.getStdin();
  }

  protected OutputPort getStdout() throws IOException {
    return mEnvironment.getStdout();
  }

  protected IOutputPort getStderr() throws IOException {
    return mEnvironment.getStderr();
  }

  /**
   * @return
   * @see org.xmlsh.core.XEnvironment#getCurdir()
   */
  public File getCurdir() {
    return mEnvironment.getCurdir();
  }

  public String getAbsoluteURI(String sysid) throws URISyntaxException {
    return mEnvironment.getAbsoluteURI(sysid);
  }

  public InputStream getInputStream(XValue file) throws CoreException,
      IOException {
    return mEnvironment.getInputStream(file, getSerializeOpts());
  }

  public Source getSource(XValue value) throws CoreException, IOException {
    return mEnvironment.getSource(value, getSerializeOpts());
  }

  /**
   * @param file
   * @param append
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   * @throws CoreException
   * @see org.xmlsh.core.XEnvironment#getOutputStream(java.lang.String, boolean)
   */
  public OutputStream getOutputStream(String file, boolean append,
      SerializeOpts opt) throws FileNotFoundException, IOException,
      CoreException {
    return mEnvironment.getOutputStream(file, append, opt);
  }

  public InputPort getInput(XValue name) throws CoreException, IOException {
    return mEnvironment.getInput(name);
  }

  public IInputPort getInput(String name) throws CoreException, IOException {
    return mEnvironment.getInput(name);
  }

  public OutputPort getOutput(XValue name, boolean append)
      throws CoreException, IOException {
    return mEnvironment.getOutput(name, append);
  }

  public IOutputPort getOutput(String name, boolean append)
      throws CoreException, IOException {
    return mEnvironment.getOutput(name, append);
  }

  /**
   * @param s
   * @param e
   * @see org.xmlsh.core.XEnvironment#printErr(java.lang.String,
   *      java.lang.Exception)
   */
  public void printErr(String s, Exception e) {
    mEnvironment.printErr(s, e);
  }

  /**
   * @param s
   * @see org.xmlsh.core.XEnvironment#printErr(java.lang.String)
   */
  public void printErr(String s) {
    mEnvironment.printErr(s);
  }

  protected File getFile(String fname) throws IOException {
    return mShell.getFile(fname);
  }

  protected Path getPath(String fname) throws IOException {
    return FileUtils.asValidPath(getFile(fname));
  }

  protected File getFile(XValue fname) throws IOException {
    return mShell.getFile(fname);
  }

  protected Path getPath(XValue fname) throws IOException {
    return FileUtils.asValidPath(getFile(fname));
  }

  protected Shell getShell() {
    return mShell;
  }

  public void usage(String message) {
    String cmdName = getName();
    SourceLocation sloc = getLocation();
    if(!Util.isBlank(message))
      mShell.printErr(cmdName + ": " + message, sloc);
    else
      mShell.printErr(cmdName + ":", sloc);
    HelpUsage helpUsage = new HelpUsage(getShell());
    try {
      helpUsage.doUsage(mEnvironment.getStdout(), cmdName);
    } catch (Exception e) {
      mLogger.warn("Exception printing usage", e);
      mShell.printErr("Usage: <unknown>", sloc);
    }
  }

  /**
   * Note that Command usage() only prints a message ,
   * Function usage throws an exception
   */

  public void usage() {
    usage(null);
  }

  @Override
  public SourceLocation getLocation() {
    return mLocation;
  }

  @Override
  public void setLocation(SourceLocation source) {
    mLocation = source;
  }

  protected ClassLoader getClassLoader() throws CoreException {

    return mShell.getClassLoader();
  }

  protected ClassLoader getClassLoader(XValue classpath) throws CoreException {

    return getClassLoader(mShell.toUrls(classpath));
  }

  protected ClassLoader getClassLoader(List<URL> classpath)
      throws CoreException {
    return mShell.getClassLoader(classpath);
  }

  protected SerializeOpts setSerializeOpts(SerializeOpts opts) {
    return mSerializeOpts = opts;
  }

  protected SerializeOpts setSerializeOpts(Options opts)
      throws InvalidArgumentException {
    return mSerializeOpts = this.getSerializeOpts(opts);

  }

  protected SerializeOpts getSerializeOpts() {
    if(mSerializeOpts == null)
      mSerializeOpts = mShell.getSerializeOpts();
    return mSerializeOpts;
  }

  protected void error(Exception e) throws Exception {
    mShell.printErr(getName(), e);
    usage(e.toString());
    throw e;
  }

  protected void error(String s, Exception e) {
    mShell.printErr(getName() + " " + s, e);
    usage(s + " " + e.toString());
  }

  protected void requires(boolean test, String message)
      throws InvalidArgumentException {
    if(!test)
      error(message);
  }

  private void error(String message) throws InvalidArgumentException {
    mShell.printErr(getName() + " " + message);
    usage(message);
    throw new InvalidArgumentException(message);

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
