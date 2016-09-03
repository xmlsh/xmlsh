/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public class Options implements Cloneable {

  /*
   * A single option is of the form [+]short[=long][:[+]] Multiple options are
   * separated by ","
   *
   * [+] If option starts with a "+" then it is a boolean option that at
   * runtime can start with a + or -. for example cmd +opt short The short
   * form of the option. Typically a single letter =long The long form of the
   * option. Typically a word [:[+]] If followed by a ":" then the option is
   * required to have a value which is taken from the next arg If followed by
   * a ":+" then the option can be specified multiple times
   *
   *
   * Examples
   *
   * a Single optional option "-a" a=all Long form accepted either "-a" or
   * "-all" +v=verbose Long or short form may be specified with - or + e.g. -v
   * or +verbose i: Option requires a value. e.g -i inputfile i:+ Option may
   * be specified multiple times with values. e.g. -i input1 -i input2
   */

  public static class OptionDef implements Cloneable {
    private String name; // short name typically 1 letter
    private String longname; // long name/alias
    private boolean expectsArg; // expects an argument
    private boolean multiple; // may occur multiple times
    private boolean flag; // may be preceeded by +

    public OptionDef(String name, String longname, boolean arg,
        boolean multi, boolean plus) {
      setName(name);
      setLongname(longname);
      setExpectsArg(arg);
      setMultiple(multi);
      setFlag(plus);
    }

    OptionDef() {
      name = longname = "-";
      expectsArg = false;
      multiple = false;
      flag = false;

    }

    // Clone
    public OptionDef(OptionDef that) {
      this(that.name, that.longname, that.expectsArg, that.multiple, that.flag);
    }

    public OptionDef copy() {
      return new OptionDef(this);
    }

    @Override
    public Object clone() {
      return copy();

    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getLongname() {
      return longname;
    }

    public void setLongname(String longname) {
      this.longname = longname;
    }

    public boolean isExpectsArg() {
      return expectsArg;
    }

    public void setExpectsArg(boolean expectsArg) {
      this.expectsArg = expectsArg;
    }

    public boolean isMultiple() {
      return multiple;
    }

    public void setMultiple(boolean multiple) {
      this.multiple = multiple;
    }

    public boolean isFlag() {
      return flag;
    }

    public void setFlag(boolean flag) {
      this.flag = flag;
    }

    @Override
    public boolean equals(Object that) {
      if(!(that instanceof OptionDef))
        return false;

      OptionDef othat = (OptionDef) that;
      if(this == that)
        return true;

      return name.equals(othat.name) && longname.equals(othat.longname)
          && expectsArg == othat.expectsArg
          && multiple == othat.multiple && flag == othat.flag;
    }

    public boolean isOption(String str) {
      assert (str != null);
      return Util.isEqual(str, name) || Util.isEqual(str, longname);
    }

  }

  static OptionDef mDashDash = new OptionDef();

  public static class OptionValue {
    private OptionDef option;
    private boolean optflag = true; // true if '-' , false if '+'
    private XValue value = null;

    OptionValue() {
      option = mDashDash;

    }

    OptionValue(OptionDef def, boolean flag) {
      option = def;
      optflag = flag;
    }

    // Set a single value
    void setValue(XValue v) throws UnexpectedException,
        InvalidArgumentException {
      if(option.isExpectsArg())
        value = v;
      else if(option.isFlag())
        optflag = v.toBoolean() ? true : false;
      else
        throw new UnexpectedException("Unexpected use of option: "
            + option.name);
    }

    /**
     * @return the option
     */
    public OptionDef getOptionDef() {
      return option;
    }

    /**
     * @return the arg
     */
    public XValue getValue() {
      return option.isExpectsArg() ? value : XValue.newXValue(optflag);
    }

    public boolean getFlag() {
      return optflag;
    }

    public String toStringValue() {
      return getValue().toString();
    }

  }

  @SuppressWarnings("serial")
  public static class OptionDefs extends ArrayList<OptionDef>
      implements Cloneable {
    public OptionDefs() {
      super();
    }

    public OptionDefs(OptionDefs that) {
      that.forEach(o -> super.add(o.copy()));
    }

    public OptionDefs(String sdefs) {
      this(parseDefs(sdefs));
    }

    public OptionDefs(List<? extends OptionDef> c) {
      for(OptionDef d : c)
        addOptionDef(d);
    }

    public OptionDefs(OptionDef o) {
      super.add(o); // Safe - only 1 option
    }

    public OptionDefs addOptionDef(OptionDef def) {
      return addOptionDef(def, false);
    }

    public OptionDefs addOptionDef(OptionDef def, boolean ifAbsent) {
      OptionDef exists = getOptionDef(def.getName());
      if(exists != null) {
        if(ifAbsent)
          return this;
        mLogger.warn("Redefined option def: {}", def.getName());
        remove(exists);
      }
      add(def);
      return this;
    }

    public OptionDefs(OptionDef... defs) {
      addOptionDefs(defs);
    }

    public OptionDefs addOptionDefs(OptionDefs defs) {
      return addOptionDefs(defs, false);
    }

    public OptionDefs addOptionDefsIfAbsent(OptionDefs defs) {
      return addOptionDefs(defs, true);
    }

    public OptionDefs addOptionDefs(OptionDefs defs, boolean ifAbsent) {
      for(OptionDef def : defs)
        addOptionDef(def, ifAbsent);
      return this;
    }

    public OptionDefs addOptionDefsIfAbsent(OptionDef... defs) {
      return addOptionDefs(true, defs);
    }

    public OptionDefs addOptionDefs(boolean ifAbsent, OptionDef... defs) {
      for(OptionDef od : defs)
        addOptionDef(od, ifAbsent);
      return this;

    }

    public OptionDefs addOptionDefs(OptionDef... defs) {
      for(OptionDef od : defs)
        addOptionDef(od);
      return this;
    }

    public OptionDef getOptionDef(String str) {

      for(OptionDef opt : this) {
        if(Util.isEqual(str, opt.getName())
            || Util.isEqual(str, opt.getLongname()))
          return opt;
      }
      return null;
    }

    public OptionDefs withOption(OptionDef def) {
      return addOptionDef(def);
    }

    public OptionDefs withOption(OptionDef def, boolean ifAbsent) {
      return addOptionDef(def, ifAbsent);
    }

    public OptionDefs withOptions(OptionDefs defs, boolean ifAbsent) {
      return addOptionDefs(defs, ifAbsent);
    }

    public OptionDefs withOptions(OptionDefs defs) {
      return addOptionDefs(defs);
    }

    public OptionDefs withOptions(String sdefs) {
      return withOptions(parseDefs(sdefs));
    }

    public OptionDefs withOptions(String sdefs, boolean ifAbsent) {
      return withOptions(parseDefs(sdefs), ifAbsent);
    }

    public static OptionDef parseDef(String sdef) {
      boolean bHasArgs = false;
      boolean bHasMulti = false;
      boolean bPlus = false;

      if(sdef.startsWith("+")) {
        bPlus = true;
        sdef = sdef.substring(1);
      }
      else

      if(sdef.endsWith(":")) {
        sdef = sdef.substring(0, sdef.length() - 1);
        bHasArgs = true;
      }
      else if(sdef.endsWith(":+")) {
        sdef = sdef.substring(0, sdef.length() - 2);
        bHasArgs = true;
        bHasMulti = true;
      }

      // Check for optional long-name
      // a=longer
      StringPair pair = new StringPair(sdef, '=');
      if(pair.hasDelim())
        return new OptionDef(pair.getLeft(), pair
            .getRight(), bHasArgs, bHasMulti, bPlus);
      else
        return new OptionDef(sdef, null, bHasArgs,
            bHasMulti, bPlus);
    }

    public static OptionDefs parseDefs(String sdefs) {

      OptionDefs defs = new OptionDefs();
      String[] adefs = sdefs.trim().split("\\s*,\\s*");
      for(String sdef : adefs) {
        defs.addOptionDef(parseDef(sdef));
      }
      return defs;
    }

    public static OptionDefs parseDefs(String defs1, String... defsv) {
      OptionDefs defs = parseDefs(defs1);
      for(String sd : defsv)
        defs.addOptionDef(parseDef(sd));
      return defs;
    }

  }

  private OptionDefs mDefs;
  private List<XValue> mRemainingArgs;
  private List<OptionValue> mOptions;

  static Logger mLogger = LogManager.getLogger();

  /*
   * Parse a string list shorthand for options defs "a,b:,cde:" =>
   * ("a",false),("b",true),("cde",true)
   */

  public boolean hasDashDash() {
    return hasOpt(mDashDash);
  }

  public static OptionDefs parseDefs(String sdefs) {
    return OptionDefs.parseDefs(sdefs);
  }

  public Options(String options) {
    this(parseDefs(options));
  }

  public Options(OptionDefs... options) {
    mDefs = new OptionDefs();
    for(OptionDefs o : options) {
      mDefs.addOptionDefs(o);
    }
  }

  // @Depreciated
  public Options(String option_str, OptionDefs option_list) {
    this(parseDefs(option_str).withOptions(option_list));
  }

  // Default constructor
  public Options() {
    mDefs = new OptionDefs();
  }

  public OptionDefs addOptionDefs(String option_str, boolean ifAbsent) {
    OptionDefs option_list = parseDefs(option_str);
    addOptionDefs(option_list, ifAbsent);
    return option_list;
  }

  public OptionDef addOptionDef(String option_str, boolean ifAbsent) {
    OptionDef opt = parseDef(option_str);
    addOptionDef(opt, ifAbsent);
    return opt;
  }

  private OptionDef parseDef(String option_str) {
    return OptionDefs.parseDef(option_str);
  }

  public Options addOptionDefs(OptionDefs option_list, boolean ifAbsent) {
    mDefs.addOptionDefs(option_list, ifAbsent);
    return this;
  }

  public Options withOptionDef(OptionDef def, boolean ifAbsent) {
    return addOptionDef(def, ifAbsent);
  }

  public Options withOptionDefs(OptionDefs def, boolean ifAbsent) {
    return addOptionDefs(def, ifAbsent);
  }

  public Options addOptionDef(OptionDef def, boolean ifAbsent) {
    mDefs.addOptionDef(def, ifAbsent);
    return this;
  }

  public OptionDef getOptDef(String str) {
    assert (mDefs != null);
    return mDefs.getOptionDef(str);

  }

  public Options withDefaultOptions(List<OptionValue> defaults) {
    defaults.forEach(
        v -> {
          OptionValue ev = this.getOpt(v.getOptionDef());
          if(ev == null)
            mOptions.add(v);
        });
    return this;
  }

  public Options withDefaultArgs(List<XValue> defaults)
      throws UnexpectedException, InvalidArgumentException, UnknownOption {
    if(defaults == null || defaults.isEmpty())
      return this;
    return withDefaultOptions(
        parseValues(mDefs, defaults, new ArrayList<XValue>(), true));
  }

  public Options parse(List<XValue> args, List<XValue> defaults)
      throws UnknownOption, UnexpectedException, InvalidArgumentException {
    return parse(args).withDefaultArgs(defaults);
  }

  public Options parse(List<XValue> args)
      throws UnknownOption, UnexpectedException, InvalidArgumentException {
    return parse(args, false);
  }

  public Options parse(List<XValue> args, boolean stopOnUnknown)
      throws UnexpectedException, InvalidArgumentException, UnknownOption {
    mOptions = parseValues(mDefs, args,
        mRemainingArgs = new ArrayList<XValue>(), stopOnUnknown);
    return this;
  }

  // Parse but do not store into mOptions
  public static List<OptionValue> parseValues(OptionDefs defs,
      List<XValue> args,
      List<XValue> remainingArgs, boolean stopOnUnknown) throws UnknownOption,
      UnexpectedException, InvalidArgumentException {
    List<OptionValue> options = new ArrayList<>();
    Iterator<XValue> I = args.iterator();
    while(I.hasNext()) {
      XValue arg = I.next();

      String sarg = (arg.isAtomic() ? arg.toString() : null);

      if(sarg != null && (sarg.startsWith("-") || sarg.startsWith("+"))
          && !sarg.equals("--") && !Util.isInt(sarg, true)) {
        String a = sarg.substring(1);
        char flag = sarg.charAt(0);

        OptionDef def = defs.getOptionDef(a);
        if(def == null) {
          if(stopOnUnknown) {
            remainingArgs.add(arg);
            break;
          }
          throw new UnknownOption("Unknown option: " + a);
        }
        if(flag == '+' && !def.isFlag())
          throw new UnknownOption("Option : " + a
              + " cannot start with +");

        boolean bRepeat = hasOpt(options, def);

        if(bRepeat && !def.isMultiple())
          throw new UnknownOption(
              "Unexpected multiple use of option: " + arg);
        OptionValue ov = new OptionValue(def, flag == '-');
        if(def.isExpectsArg()) {
          if(!I.hasNext())
            throw new UnknownOption("Option has no args: " + arg);
          ov.setValue(I.next());
        }
        options.add(ov);

      }
      else {

        if(arg.isAtomic() && arg.equals("--")) {
          arg = null;
          options.add(new OptionValue());
        }
        if(arg != null)
          remainingArgs.add(arg);

        break;

      }

    }
    while(I.hasNext())
      remainingArgs.add(I.next());
    return options;

  }

  public List<OptionValue> getOpts() {
    return mOptions;
  }

  private static OptionValue getOpt(List<OptionValue> opts, OptionDef def) {
    assert (def != null);
    assert (opts != null);
    for(OptionValue ov : opts) {
      if(ov.option.equals(def))
        return ov;
    }
    return null;
  }

  public OptionValue getOpt(OptionDef def) {
    return getOpt(mOptions, def);
  }

  private static boolean hasOpt(List<OptionValue> opts, OptionDef def) {
    return getOpt(opts, def) != null;
  }

  public boolean hasOpt(OptionDef def) {
    return getOpt(mOptions, def) != null;
  }

  public OptionValue getOpt(String opt) {
    for(OptionValue ov : mOptions) {
      if(ov.getOptionDef().isOption(opt))
        return ov;
    }
    return null;
  }

  public boolean hasOpt(String opt) {
    return getOpt(opt) != null;

  }

  public boolean getOptFlag(String opt, boolean defValue) {
    OptionValue value = getOpt(opt);
    if(value == null)
      return defValue;
    else
      return value.getFlag();
  }

  public String getOptString(String opt, String defValue) {
    OptionValue value = getOpt(opt);
    if(value != null)
      return value.toStringValue();
    else
      return defValue;

  }

  public String getOptStringRequired(String opt)
      throws InvalidArgumentException {
    OptionValue value = getOpt(opt);
    if(value != null)
      return value.getValue().toString();

    throw new InvalidArgumentException("Required option: -" + opt);

  }

  public boolean getOptBool(String opt, boolean defValue) {
    OptionValue value = getOpt(opt);
    if(value != null)
      try {
        return value.getValue().toBoolean();
      } catch (Exception e) {
        return false;
      }
    return defValue;

  }

  public List<XValue> getRemainingArgs() {
    if(mRemainingArgs == null)
      mRemainingArgs = new ArrayList<XValue>(0);
    return mRemainingArgs;
  }

  public XValue getOptValue(String arg) {
    OptionValue ov = getOpt(arg);
    if(ov == null)
      return null;
    else if(ov.getOptionDef().isMultiple())
      return XValue.newXValue(getOptValues(arg));
    else
      return ov.getValue();
  }

  public XValue getOptValueRequired(String arg)
      throws InvalidArgumentException {
    OptionValue ov = getOpt(arg);
    if(ov == null)
      throw new InvalidArgumentException("Required option: -" + arg);
    if(ov.getOptionDef().isMultiple())
      return XValue.newXValue(getOptValues(arg));
    else
      return ov.getValue();
  }

  public List<XValue> getOptValuesRequired(String arg)
      throws InvalidArgumentException {

    List<XValue> values = getOptValues(arg);
    if(values == null || values.isEmpty())
      throw new InvalidArgumentException("Required option: -" + arg);

    return values;
  }

  public List<XValue> getOptValues(String arg) {
    ArrayList<XValue> values = new ArrayList<>();
    for(OptionValue ov : mOptions) {
      if(ov.getOptionDef().isOption(arg))
        values.add(ov.getValue());

    }
    return values.isEmpty() ? null : values;
  }

  public boolean hasRemainingArgs() {
    return mRemainingArgs != null && !mRemainingArgs.isEmpty();
  }

  public double getOptDouble(String opt, double def) {
    return Util.parseDouble(getOptString(opt, ""), def);
  }

  public int getOptInt(String opt, int def) {
    return Util.parseInt(getOptString(opt, ""), def);
  }

  public long getOptLong(String opt, long l) {
    return Util.parseLong(getOptString(opt, ""), l);
  }

  /**
   * @return the defs
   */
  public OptionDefs getOptDefs() {
    return mDefs;
  }

  public static String joinOptions(String... sopts) {
    if(sopts == null || sopts.length == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    for(String s : sopts) {
      if(!Util.isBlank(s)) {
        if(sb.length() > 0)
          sb.append(",");
        sb.append(s);
      }
    }
    return sb.toString();
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
