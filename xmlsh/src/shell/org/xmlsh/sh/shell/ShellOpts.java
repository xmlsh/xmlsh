/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.shell;

import java.util.EnumSet;

import org.apache.logging.log4j.Level;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.util.Util;

public class ShellOpts
{
  private static final String XLOCFORMAT = "XLOCFORMAT";
  public static final String SHELL_OPTS = "+x,+v,+xpipe,+e,+location,location-format:,+trace,trace-file:,o:,+a,trace-level:";
  public boolean mVerbose = false;		// -v
  public boolean mExec = false;		// -x
  public boolean mXPipe = false;		// -xpipe
  public boolean mThrowOnError = false;		// -e
  public boolean mLocation = true;       // print location on error
  public boolean mLocationFormat = defaultLocationFormat();      // TODO : convert to enum
  public boolean mTrace = false ;
  public String  mTraceFile = null;
  public boolean mAllLocal = false;
  public boolean mAllExport = false ;
  public String  mLocalMatch = null ;
  

  SerializeOpts mSerialize;
  public Level mTraceLevel = Level.INFO ;

  public ShellOpts()
  {
    mSerialize = new SerializeOpts();
  }

  public static final boolean defaultLocationFormat()
  {
    String format = System.getenv(XLOCFORMAT);
    return Util.parseBoolean(format, false);
  }

  public ShellOpts(ShellOpts that)
  {

    mVerbose = that.mVerbose;
    mExec = that.mExec;
    mXPipe = that.mXPipe;
    mThrowOnError = that.mThrowOnError;
    mLocation = that.mLocation;
    mSerialize = new SerializeOpts(that.mSerialize);
    mLocationFormat = that.mLocationFormat;
    mTrace = that.mTrace;
    mTraceFile = that.mTraceFile;
  //  mAllLocal = that.mAllLocal;
  //  mAllExport = that.mAllExport;
    mLocalMatch = that.mLocalMatch ;
  }

  public void setOption(String opt, boolean on)
  { 
       
    switch( opt ){
    case "x" :
      mExec = on;
      break;
    case "v":
      mLocation = mVerbose = on;
    break; case "xpipe":
      mXPipe = on;
    break; case "e":
      mThrowOnError = on;
    break; case "location":
      mLocation = on;
    break; 
      case "trace":
        mTrace = on ;
        break;
  case "local" :
      mAllLocal=true;
      break;
  case "nolocal" :
      mAllLocal=false ;
      break;
  default : 
    mSerialize.setOption(opt, on);
    }
    
  }

  public void setOption(String opt, XValue value) throws InvalidArgumentException
  {

    // No shell options take a string value so just defer to serialization options

    assert( value.isAtomic() );
    switch(opt){
    case "location-format" :
      mLocationFormat = Util.parseBoolean(value.toString(),mLocationFormat);
      break;
    case "trace-file" :
      mTraceFile = value.toString();
      break;
    case "trace-level" :
      mTraceLevel = Level.toLevel(value.toString());
      break;
    case "o" : 
      setOption( value.toString() , true );
      break ;
    default:
       mSerialize.setOption(opt, value);
    }

  }

  public Level getTraceLevel() {
    return mTraceLevel;
}

  public boolean isAllLocal(){
      return mAllLocal;
  }
  public boolean isTraceEnabled() {
      return mTrace ;
  }
public void setOption(OptionValue ov) throws InvalidArgumentException
  {

    if(ov.getOptionDef().isExpectsArg())
      setOption(ov.getOptionDef().getName(), ov.getValue());
    else
    if( ov.getOptionDef().isFlag() )
        setOption(ov.getOptionDef().getName(), ov.getFlag());
    
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
