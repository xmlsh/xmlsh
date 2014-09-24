/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueList;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariableExpr;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.CharAttr;
import org.xmlsh.sh.shell.Expander;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

public class EvalUtils
{

  /*
   * Eval a var expression ${ [ prefix ] varname [ '[' ind ']' ] [':' suffix ] }
   */
  public static XValue evalVar(Shell shell, EvalEnv env, XVariableExpr expr) throws IOException, CoreException
  {

    XVariable var = shell.getEnv().getVar(expr.getName());
    if(var == null) {
      // Special case ${#x} == 0 if x is undef
      if(Util.isEqual(expr.getPrefix(), "#")) {
        return XValue.newXValue(0);
      }
      return null;

    }
    // ${#var} notation
    if(Util.isEqual(expr.getPrefix(), "#")) {
      return XValue.newXValue(var.getSize());
    }

    if(Util.isBlank(expr.getIndex()) && Util.isBlank(expr.getField()))
      return var.getValue();
    else return var.getValue(shell, env, expr.getIndex(), expr.getField());
  }

  /*
   * Evaluate a variable expression and extract its value
   */

  private static XVariableExpr parseVarExpr(Shell shell, EvalEnv env, String varname)
      throws IOException, CoreException
      {

    XVariableExpr expr = new XVariableExpr();
    // ${#var} notation
    if(varname.startsWith("#") && varname.length() > 1) {
      varname = varname.substring(1);
      expr.setPrefix("#");
    }

    // Get the XVariable
    String ind = null; // [ind] expr
    String field = null; // :tie expr

    // Strip off tie expr
    if(varname.contains(":")) {
      int as = varname.indexOf(':');
      if(as > 0) {
        expr.setField(varname.substring(as + 1));
        varname = varname.substring(0, as);
      }
    }

    // Look for array notation
    // ${var[3]}
    if(varname.contains("[")) {
      int as = varname.indexOf('[');
      ind = varname.substring(as + 1, varname.indexOf(']')).trim();
      // Special case - expand index as variables ... really shouldnt do this
      if(ind.startsWith("$"))
        ind = expandStringToString(shell, ind, env);
      expr.setIndex(ind);
      varname = varname.substring(0, as);
    }

    expr.setName(varname);
    return expr;

      }

  public static XValue getIndexedValue(EvalEnv env, XValue xvalue, String ind) throws CoreException
  {
    assert (xvalue != null);
    assert (!Util.isBlank(ind));

    if(xvalue == null)
      return XValue.nullValue();
    if(Util.isBlank(ind))
      return xvalue;
    return xvalue.getTypeMethods().getXValue(xvalue.asObject(), ind);
  }

  public static XValue getIndexedValue(EvalEnv env, XValue xvalue, int index) throws CoreException
  {
    assert (xvalue != null);
   // assert (index >= 0);

    if(xvalue == null)
      return XValue.nullValue();
    if(index < 0)
      return XValue.nullValue();
    //  throw new InvalidArgumentException("Invalid index for indexed expression: " + index);

    return xvalue.getTypeMethods().getXValue(xvalue.asObject(), index);
  }

  /*
   * Recursively Expand a possibly multi-level wildcard rooted at a directory
   */
  public static List<String> expandDir(File dir, CharAttributeBuffer wild, boolean bDirOnly)
  {
    ArrayList<String> results = new ArrayList<String>();
    /*
     * special case for "." and ".." as the directory component
     * They dont show up in dir.list() so should always be considered an exact match
     */
    if(wild.stringEquals(".") || wild.stringEquals("..")) {
      results.add(wild.toString());
      return results;
    }

    boolean bIsWindows = Util.isWindows();
    boolean caseSensitive = !bIsWindows;

    /*
     * Hack to handle 8.3 windows file names like "Local~1"
     * If not matched and this is windows
     * try an exact match to the canonical expanson of the dir and wild
     */
    if(bIsWindows && wild.indexOf(0, '~', CharAttr.ATTR_NONE.attr()) >= 0) {
      String wildString = wild.toString();
      File fwild = new File(dir, wildString);
      if(fwild.exists()) {
        results.add(wildString);
        return results;
      }
    }

    String[] files = dir.list();

    Pattern wp = Util.compileWild(wild, CharAttr.ATTR_ESCAPED.attr(), caseSensitive);

    for (String f : files) {

      boolean bMatched = wp.matcher(f).matches();

      if(bMatched && (bDirOnly ? (new File(dir, f).isDirectory()) : true)) {
        results.add(f);
      }
    }
    if(results.size() == 0)
      return null;
    Collections.sort(results);
    return results;

  }

  public static void expandDir(File dir, String parent, CharAttributeBuffer wilds[], List<String> results)
  {
    CharAttributeBuffer wild = wilds[0];
    if(wilds.length < 2)
      wilds = null;
    else wilds = Arrays.copyOfRange(wilds, 1, wilds.length);

    List<String> rs = EvalUtils.expandDir(dir, wild, wilds != null);
    if(rs == null)
      return;
    for (String r : rs) {
      String path = parent == null ? r : parent + (parent.endsWith("/") ? "" : "/") + r;

      if(wilds == null)
        results.add(path);
      else expandDir(new File(dir, r), path, wilds, results);

    }
  }

  public static ParseResult expandStringToResult(Shell shell, String value, EvalEnv env, ParseResult result) throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    return e.expandStringToResult(value, env, result == null ? new ParseResult(shell) : result);

      }

  public static ParseResult expandValueToResult(Shell shell, XValue xv, EvalEnv env, ParseResult result) throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    return e.expandValueToResult(xv, env, result == null ? new ParseResult(shell) : result);
      }

  public static List<XValue> expandResultToList(Shell shell, ParseResult result, EvalEnv env)
      throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    return e.expandResultToList(env, result);
      }

  public static List<XValue> expandValueToList(Shell shell, XValue xv, EvalEnv env)
      throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    return e.expandResultToList(env, e.expandValueToResult(xv, env, new ParseResult(shell)));
      }

  public static List<XValue> expandStringToList(Shell shell, String s, EvalEnv env)
      throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    return e.expandStringToList(s, env);
      }

  public static String expandStringToString(Shell shell, String value, EvalEnv env)
      throws IOException, CoreException
      {
    List<XValue> ret = expandStringToList(shell, value, env);
    if(ret.size() == 0)
      return "";
    else if(ret.size() == 1)
      return ret.get(0).toString();
    return Util.joinValues(ret, ShellConstants.ARG_SEPARATOR);

      }

  // Expand a word and return as a single XValue
  // Preserves sequences and expands
  public static XValue expandStringToValue(Shell shell, String value, EvalEnv env)
      throws IOException, CoreException
      {
    List<XValue> ret = expandStringToList(shell, value, env);
    return expandListToValue(env, ret);

      }

  // Converts a List<XValue> into single XValue
  public static XValue expandListToValue(EvalEnv env, List<XValue> ret)
  {
    if(ret == null || ret.size() == 0)
      return env.omitNulls() ? XValue.nullValue() : XValue.empytSequence() ;
    else if(ret.size() == 1)
      return ret.get(0);

    return XValue.newXValue(ret);
  }

  public static ParseResult expandListToResult(Shell shell, List<XValue> list, EvalEnv env)
      throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    ParseResult result = new ParseResult(shell);
    for (XValue xv : list)
      result = e.expandValueToResult(xv, env, result);
    return result;
      }

  public static XValue expandResultToValue(Shell shell, ParseResult result, EvalEnv env)
      throws IOException, CoreException
      {
    List<XValue> ret = expandResultToList(shell, result, env);
    return expandListToValue(env, ret);

      }

  public static int readToMatching(String arg, int i, StringBuffer sbv, char match)
  {
    char start = arg.charAt(i++);
    int matchCount = 1;

    // Eat up to match char '}'
    for (; i < arg.length(); i++) {
      char c = arg.charAt(i);
      if(c == match) {
        if(--matchCount == 0)
          break;
      }
      else if(c == start)
        matchCount++;

      sbv.append(c);

    }
    return i;
  }

  public static XValue splitStringToValue(Shell shell, String word, EvalEnv env) throws IOException
  {
    assert( word != null );
    if( word == null|word.isEmpty() )
      return XValue.newXValue(word);
    
    
    // if expand word then need to do IFS splitting
    if(env.expandWords() && !env.preserveValue())
      return XValue.newXValue((String[]) shell.getIFS().split(word).toArray());
    else return XValue.newXValue(word);
  }

  public static ParseResult splitStringToResult(Shell shell, String word, EvalEnv env, ParseResult result) throws IOException, CoreException
      {
    Expander e = new Expander(shell);
    // if expand word then need to do IFS splitting
    if(env.expandWords() && !env.preserveValue()) {
      for (String s : shell.getIFS().split(word))
        result = e.expandStringToResult(s, env, result);
    }
    else e.expandStringToResult(word, env, result);
    return result;

      }

  /*
   * Evaluate a variable and return either a list of zero or more values
   */
  public static ParseResult evalVarToResult(Shell shell, XVariableExpr expr, EvalEnv env, CharAttr attr,
      ParseResult result) throws IOException, CoreException
      {
    List<XValue> vs = null;
/*
 * if( expr.getName().equals("*")){
 * // Add all positional variables as args
 * boolean bFirst = true ;
 * for( XValue v : shell.getArgs() ){
 * if( attr.isSoftQuote() ) {
 * if( ! bFirst )
 * result.append( shell.getIFS().getFirstChar() , attr );
 * result.append( v , env , attr );
 * }
 * else
 * {
 * result.flush();
 * result.add( v );
 * }
 * bFirst = false ;
 * }
 * return result ;
 * 
 * }
 * else
 * if( expr.getName().equals("@")){
 * // Add all positional variables as args except "$@" with 0 args is dropped
 * boolean bFirst = true ;
 * List<XValue> args = shell.getArgs();
 * if( args.isEmpty() )
 * result.resetIfEmpty();
 * else
 * for( XValue v : args ){
 * if( attr.isSoftQuote() && bFirst )
 * result.append( v , env , attr );
 * else
 * result.add( v );
 * bFirst = false ;
 * }
 * return result ;
 * }
 */

    // TODO: Special case of $@ in quotes

    boolean dollarAt = expr.getName().equals("@");
    if(attr.isQuote() && dollarAt)
      vs = shell.getArgs();

    else {
      XValue v = evalVar(shell, env, expr);
      if(v == null)
        return result;
      // Non tong null values go away
      else if(!attr.isPreserve() && v.isNull())
        vs = null;
      
      if(!isExpandable(v, env))
        vs = Collections.singletonList(v);

      else
      if(! attr.isQuote() && ( v.isSequence() ||  ! env.expandAny()) ) {  // $* $@ or other sequence like lists
         vs = v.asXList();
      }
      else {
        List<String> fields;
        if(attr.isQuote() || ! isExpandable(v, env) )
          vs = Collections.singletonList(v);

        else {
          String s = v.toString();
          if(env.expandWords()) {
            // Extract fields
            fields = shell.getIFS().split(s);
            if(Util.isEmpty(fields))
              vs = null;

            // Try to preserve original value
            else if(fields.size() == 1 && Util.isEqual(fields.get(0), s))
              vs = Collections.singletonList(v);

            else {
              vs = new ArrayList<XValue>(fields.size());
              for (String f : fields) {
                if(Util.isEmpty(f))
                  continue;
                vs.add(XValue.newXValue(f));
              }
            }
          }
          else vs = Collections.singletonList(XValue.newXValue(s));
        }
      }
    }

    // Append the first value to any previous content in the arg
    // N..last-1 become new args
    // Last is a new word but possibly appendable

    if(vs != null) {
      int vsize = vs.size();
      for (int vi = 0; vi < vsize; vi++) {
        XValue xv = vs.get(vi);
        if(vi > 0)
          result.flush();
        result.append(xv, env, attr);
      }
    }
    return result;

      }

  // What kinds of values do we peek into
  private static boolean isExpandable(XValue v, EvalEnv env)
  {
    if(env.preserveValue() || ! env.expandAny() )
      return false;
    /*
    if( v.isAtomic()   )
      return true ;
    
    // HACK
    //
    if( v.isXdmValue() || (v.isXType() && v.isInstanceOf(XValueSequence.class) ) )
      return true;
    
    return false ;
*/
    return v.isAtomic()  ||  v.isSequence() ;
  }

  public static ParseResult evalVarToResult(Shell shell, String var, EvalEnv env, CharAttr attr, ParseResult result)
      throws IOException, CoreException
      {

    if(Util.isOneOf(var,"*", "@")) {
      XVariableExpr expr = new XVariableExpr();
      expr.setName(var);
      return evalVarToResult(shell, expr, env.withFlagsOff(EvalFlag.SPLIT_WORDS , EvalFlag.EXPAND_VAR), attr, result);
    }

    XVariableExpr expr = parseVarExpr(shell, env, var);
    return evalVarToResult(shell, expr, env, attr, result);

      }

  public static int getSize(XValue xvalue)
  {
    if(xvalue == null || xvalue.isNull())
      return 0;
    return xvalue.getTypeMethods().getSize(xvalue.asObject());
  }

  public static XValue newContainerInstance(TypeFamily family)
  {
    switch (family) {
    case XTYPE:
    case JAVA:
      return XValue.newXValue(family, new XValueList());
    case JSON:
      return XValue.newXValue(family, JSONUtils.newJsonObject());

    case XDM:
      return XValue.newXValue(family, XMLUtils.emptySequence());

    }
    return XValue.nullValue();
  }

  public static XValue getValues(EvalEnv env, XValue xvalue)
  {
    if(xvalue == null || xvalue.isNull())
      return XValue.nullValue();

    List<XValue> xvs = xvalue.getTypeMethods().getXValues(xvalue.asObject());
    return expandListToValue(env, xvs);

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