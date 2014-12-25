/**
 * $Date$
 * 
 */

package org.xmlsh.sh.shell;

import static org.xmlsh.sh.shell.CharAttr.ATTR_ESCAPED;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.types.XDMTypeFamily;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ThreadLocalShell;

public class Expander
{
  private static Logger mLogger = LogManager.getLogger();

  public Shell mShell;

  /*
   * Attribute enums
   */

  /* this"Is a 'string\' in' a $var "string *.x "*.y" \*.z */

  public Expander(Shell shell)
  {
    mShell = shell;
  }

  /*
   * Expand a value into a Results buffer
   * Used for combining possible joined values by repeated calls
   */
  public ParseResult expandValueToResult(XValue value, EvalEnv env, ParseResult result) throws IOException,
      CoreException
  {
    assert (result != null);
    if(!env.preserveValue() && value.isAtomic())
      result = expandStringToResult(value.toString(), env, result);
    else result.add(value, env.preserveValue());
    return result;
  }

  /*
   * Expand a string to a list of XValues by
   * 1) Parsing Quotes
   * 2) Expanding Variables
   * 3) Tokenizing by IFS (expand word) and combining adjacent words
   * 4) globbing
   */

  public List<XValue> expandStringToList(String arg, EvalEnv env) throws IOException, CoreException
  {
    ParseResult result = new ParseResult();
    return expandResultToList(env, expandStringToResult(arg, env, result));

  }

  public List<XValue> expandResultToList(EvalEnv env, ParseResult result)
  {
	
	mLogger.entry(env, result);
    assert (result != null);
    List<XValue> xvresult = result.expandWild(env, mShell.getCurdir());
    if(xvresult == null)
      return null;

    if(env.expandSequences())
      xvresult = Util.expandSequences(xvresult);
    else xvresult = Util.combineSequence(xvresult);

    return mLogger.exit(xvresult);
  }

  /*
   * Expand a single word value :
   * <{{ ... }}> is a hard multi line quote with nothing inside touched
   * <[ ... ]> is an XQuery expression
   * $xxx variable expressions
   * "literal" simpmle literal
   * "$var literal $var" quoted mixed literal
   * 
   * If bPreserve is set then this is inside {value} which is used to eval/expand the value but do NO substitution
   * NO globbing is done
   */
  public ParseResult expandStringToResult(String arg, EvalEnv env, ParseResult result) throws IOException,
      CoreException
   {
	  
	mLogger.entry(arg, env, result);

    assert (result != null);
    
    CharAttrs curAttr = env.asCharAttrs();
    
    
    // Special case for "$@" which does NOT 'stringify' arguments
    /*
    if( arg.equals("\"$@\"")){
    	
        result = EvalUtils.evalVarToResult(mShell, "@", env, curAttr , result);
        if(curAttr.isSoftQuote())
            result.resetIfEmpty();
        return result ;
    	
    } */
    

    // <{ big quotes }>
    if(arg.startsWith("<{{") && arg.endsWith("}}>")) {
      // Add as a raw value
      result.add(XValue.newXValue(arg.substring(3, arg.length() - 3)), true);
      return mLogger.exit(result);
    }

    // <[ XEXPR ]>
    if(arg.startsWith("<[") && arg.endsWith("]>")) {
      result.add(parseXExpr(mShell, arg.substring(2, arg.length() - 2)), true);
      return mLogger.exit(result);
    }

    char c;
    int i;


    for (i = 0; i < arg.length(); i++) {

      c = arg.charAt(i);

      if(env.parseQuotes()) {
        // Quote - if in quotes then clear only the matching quote
        if(CharAttrs.isQuote(c)) {
          CharAttrs ca = CharAttrs.valueOf(c);
          if(curAttr.isQuote()) { // in quotes
            curAttr.clear(ca);
            if(curAttr.isQuote())
              result.append(c, curAttr);
          }
          else {
            result.append((String) null, curAttr);
            curAttr.set(ca);
          }
          continue;
        }

        // Escape
        // foo\bar -> foobar
        // "foo\bar" -> "foo\bar"
        // "foo\\bar" -> "foo\bar"
        // 'foo\\bar' -> 'foo\\bar'

        /*
         * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html
         */
        else if(c == '\\') {
          if(curAttr.isHardQuote())
            result.append(c, curAttr);
          else

          if(i < arg.length()) {
            char nextc = arg.charAt(++i);
            if(curAttr.isSoftQuote()) {
              switch (nextc) {
              case '$':
              case '`':
              case '"':
              case '\\':
              case '\n':
                break;
              default:
                result.append(c, curAttr);
                break;
              }
            }

            // For one char we escape
            CharAttrs cAttr = CharAttrs.newInstance(ATTR_ESCAPED);
            cAttr.set(curAttr);
            result.append(nextc, cAttr);
          }
          continue;
        }

      }

      if(!curAttr.isHardQuote() && c == '$') {
        if(++i == arg.length()) {
          result.append('$', curAttr); // Special case of a single "$"
          break;
        }

        StringBuffer sbv = new StringBuffer();
        if(arg.charAt(i) == '{') {
          i = EvalUtils.readToMatching(arg, i, sbv, '}');
        }

        else {
          // Speical case
          // $? $* $@ $$ $0...$9
          c = arg.charAt(i);
          if(c == '?' || c == '@' || c == '$' || c == '#' || c == '*' || c == '!' || Character.isDigit(c)) {
            boolean bKeepGoing;
            do {
              bKeepGoing = false;
              sbv.append(c);

              // Special case for $<dig><dig>...
              // NOTE: Differs from sh/bsh/ksh - $11 is [arg 11] not [arg 1]1
              //
              if(Character.isDigit(c)) {
                if(i < arg.length() - 1 && Character.isDigit(c = arg.charAt(i + 1))) {
                  i++;
                  bKeepGoing = true;
                }
              }
            } while (bKeepGoing);
          }
          else {
            // Eat up all a-zA-Z_
            for (; i < arg.length(); i++) {
              c = arg.charAt(i);
              if(Util.isIdentifier(c))
                sbv.append(c);
              else {
                i--; // back up
                break;
              }
            }
          }
        }

        String var = sbv.toString();
        if(!Util.isBlank(var)) {
          // get value from single variable, parse and field split
          // guarentees no null values and empty unquoted strings were removed
          result = EvalUtils.evalVarToResult(mShell, var, env, curAttr, result);
          if(curAttr.isSoftQuote() && var.equals("@"))
            result.resetIfEmpty();

        }
        else result.append('$', curAttr);

      }
      else {
        result.append(c, curAttr);
      }

    }

    if(!env.joinValues())
      result.flush();

    return  mLogger.exit( result);

  }

  private XValue parseXExpr(Shell shell, String arg) throws CoreException
  {
 
	
	mLogger.entry(shell, arg);
    Processor processor = Shell.getProcessor();

    XQueryCompiler compiler = processor.newXQueryCompiler();
    compiler.setModuleURIResolver(new ShellModuleURIResolver(mShell));

    // Declare the extension function namespace
    // This can be overridden by user declarations
    compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);

    NameValueMap<String> ns = shell.getEnv().getNamespaces();
    if(ns != null) {
      for (String prefix : ns.keySet()) {
        String uri = ns.get(prefix);
        compiler.declareNamespace(prefix, uri);

      }

    }

    List<XValue> args = shell.getArgs();

    XQueryExecutable expr = null;
    StringBuffer sb = new StringBuffer();
    Variables variables = shell.getEnv().getVars();
    Collection<String> varnames = variables.getVarNames();

    NameValueMap<XdmValue> usedVars = new NameValueMap<XdmValue>(args.size() + varnames.size());

    for (String name : varnames) {
      XVariable var = variables.get(name);
      XdmValue xdmValue = convertVar(var);
      if(xdmValue != null) {
        usedVars.put(var.getName(), xdmValue);
        sb.append("declare variable $").append(name).append(" external ;\n");
      }
    }

    // 2010-02-04
    // Express each positional parameter as $_1 $_2 ...
    // then each
    int i = 0;
    for (XValue xv : args) {
      i++;
      XdmValue xdmValue = convertValue(xv);
      if(xdmValue != null) {
        String name = "_" + i;
        usedVars.put(name, xdmValue);
        sb.append("declare variable $" + name + " external;\n");
      }
    }

    sb.append(arg);

    Shell saved_shell = ThreadLocalShell.set(shell);
    try {
      expr = compiler.compile(sb.toString());

      XQueryEvaluator eval = expr.load();

      for (Entry<String, XdmValue> entry : usedVars.entrySet()) {
        XdmValue v = entry.getValue();
        eval.setExternalVariable(new QName(entry.getKey()), v);
      }

      XdmValue result = eval.evaluate();
      return mLogger.exit(XDMTypeFamily.getInstance().getXValue( result));
    } catch (SaxonApiException e) {
      String msg = "Error expanding xml expression: " + arg;
      mLogger.warn(msg, e);
      mShell.printErr(msg, e);
      throw new CoreException(msg, e);

    } finally {
      ThreadLocalShell.set(saved_shell);
    }

  }

  private XdmValue convertValue(XValue xv) throws InvalidArgumentException, UnexpectedException
  {
	if( xv.isNull())
		return XMLUtils.emptySequence();
    if(xv.isXdmItem() || xv.canConvert(XdmValue.class) >= 0)
      return xv.toXdmValue();
    return null;
  }

  public XdmValue convertVar(XVariable var) throws InvalidArgumentException, UnexpectedException
  {
    if(!var.isNull() )
      return convertValue(var.getValue());

    return null;
  }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

