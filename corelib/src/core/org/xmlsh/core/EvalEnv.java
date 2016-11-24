/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import static org.xmlsh.core.EvalFlag.EXPAND_SEQUENCES;
import static org.xmlsh.core.EvalFlag.EXPAND_VAR;
import static org.xmlsh.core.EvalFlag.EXPAND_WILD;
import static org.xmlsh.core.EvalFlag.SPLIT_WORDS;
import static org.xmlsh.util.CharAttr.ATTR_PRESERVE;
import java.util.EnumSet;
import org.xmlsh.sh.core.CharAttrs;
import org.xmlsh.util.Util;

/*
 * Context for evaluating a Word or Expression
 */
public class EvalEnv

{
  private EnumSet<EvalFlag> evalFlags;
  private final static EnumSet<EvalFlag> _nopreserveFlags = EvalFlag
      .evalFlags(true, true, true, false);
  private final static EnumSet<EvalFlag> _preserveFlags = EvalFlag
      .evalFlags(false, false, false, true);
  private final static EnumSet<EvalFlag> _expandFlags = EnumSet.of(EXPAND_VAR,
      EXPAND_SEQUENCES, EXPAND_WILD, SPLIT_WORDS);
  // Cached instances
  private final static EvalEnv _evalNone = new EvalEnv();
  private final static EvalEnv _basicInstance = newInstance(true, false, false,
      false);
  private final static EvalEnv _fileInstance = newInstance(false, true, true,
      false);
  private final static EvalEnv _preserveInstance = newInstance(_preserveFlags);
  private final static EvalEnv _nopreserveInstance = newInstance(
      _nopreserveFlags);

  private EvalEnv() {
    this(EvalFlag._evalFlagsNone);
  }

  @Override
  public String toString() {
    return Util.join(evalFlags);
  }

  public static EvalEnv newEnv(EvalEnv that, EnumSet<EvalFlag> flags) {
    return new EvalEnv(that, flags);

  }

  public static EvalEnv newEnv(EnumSet<EvalFlag> flags) {
    return new EvalEnv(flags);
  }

  private EvalEnv(EnumSet<EvalFlag> flags) {
    evalFlags = flags;
  }

  // Expect more then flags in the future
  private EvalEnv(EvalEnv env, EnumSet<EvalFlag> flags) {
    this(flags);
  }

  public static final EvalEnv evalNone() {
    return _evalNone;
  }

  // Hack for now
  public static EvalEnv newInstance(boolean bExpandSequences,
      boolean bExpandWild, boolean bExpandWords, boolean bPreserve) {
    return newInstance(EvalFlag.evalFlags(bExpandSequences, bExpandWild,
        bExpandWords, bPreserve));
  }

  public static EvalEnv commandArgsInstance() {

    return _nopreserveInstance;

  }

  public static EnumSet<EvalFlag> commandArgsFlags() {
    return _nopreserveFlags;
  }

  public static EnumSet<EvalFlag> returnValueMask() {
    return _preserveFlags;
  }

  public static EvalEnv newInstance(EnumSet<EvalFlag> flags) {
    return new EvalEnv(flags);
  }

  private static EvalEnv newInstance(EvalEnv that, EnumSet<EvalFlag> flags) {
    if(that.evalFlags.equals(flags))
      return that;
    return newInstance(flags);
  }

  public boolean expandVar() {
    return evalFlags.contains(EvalFlag.EXPAND_VAR);
  }

  public boolean parseQuotes() {
    return evalFlags.contains(EvalFlag.PARSE_QUOTES);
  }

  public boolean joinValues() {
    return evalFlags.contains(EvalFlag.JOIN_VALUES);
  }

  // Globbing
  public boolean expandWild() {
    return evalFlags.contains(EvalFlag.EXPAND_WILD);
  }

  // Word expansion
  public boolean expandWords() {
    return evalFlags.contains(EvalFlag.SPLIT_WORDS);
  }

  // Was tongs
  public boolean preserveValue() {
    return evalFlags.isEmpty()
        || evalFlags.equals(EnumSet.of(EvalFlag.EXPAND_VAR));

  }

  public boolean expandSequences() {
    return evalFlags.contains(EvalFlag.EXPAND_SEQUENCES);
  }

  public static EvalEnv newPreserveInstance(boolean preserve) {
    return preserve ? _preserveInstance : _nopreserveInstance;
  }

  // Basic evaluation of variables only
  public static final EvalEnv basicInstance() {
    return _basicInstance;
  }

  // Evaluation of a filename
  public static final EvalEnv fileInstance() {
    return _fileInstance;
  }

  public static EnumSet<EvalFlag> removeFlags(EvalEnv env,
      EnumSet<EvalFlag> off) {

    return Util.withEnumsRemoved(env.evalFlags, off);

  }

  public static EnumSet<EvalFlag> addFlags(EvalEnv env, EnumSet<EvalFlag> on) {
    return Util.withEnumsAdded(env.evalFlags, on);
  }

  /*
   * Fluent set
   */

  public EvalEnv withFlagSet(EvalFlag on) {
    return newInstance(this, Util.withEnumAdded(evalFlags, on));
  }

  public EvalEnv withFlagsSet(EnumSet<EvalFlag> flags) {
    return newInstance(this, Util.withEnumsAdded(evalFlags, flags));
  }

  public EvalEnv withFlagsSet(EvalFlag... flags) {
    return newInstance(this, Util.withEnumsAdded(evalFlags, flags));
  }

  public EvalEnv withFlagOff(EvalFlag off) {
    return newInstance(this, Util.withEnumRemoved(evalFlags, off));
  }

  // Allow ONLY the set of flags in mask
  public EvalEnv withFlagsMasked(EnumSet<EvalFlag> mask) {

    return newInstance(this, Util.withEnumsMasked(evalFlags, mask));
  }

  // Allow ONLY the set of flags in mask
  public EvalEnv withFlagsMasked(EvalFlag f, EvalFlag... flag) {

    return newInstance(this, Util.withEnumsMasked(evalFlags, f, flag));
  }

  public EvalEnv withFlagsOff(EvalFlag... flags) {
    return newInstance(this, Util.withEnumsRemoved(evalFlags, flags));
  }

  @Override
  public boolean equals(Object that) {
    if(that == null)
      return false;
    if(!(that instanceof EvalEnv))
      return false;
    return ((EvalEnv) that).equals(evalFlags);

  }

  public boolean isSet(EvalFlag flag) {
    return evalFlags.contains(flag);
  }

  public boolean omitNulls() {
    return evalFlags.contains(EvalFlag.OMIT_NULL);

  }

  public final CharAttrs asCharAttrs() {
    return preserveValue() ? CharAttrs.newInstance(ATTR_PRESERVE)
        : CharAttrs.newInstance();
  }

  public boolean expandAny() {
    return Util.setContainsAny(evalFlags, _expandFlags);
  }

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */
