/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import static org.xmlsh.core.XVariable.XVarFlag.EXPORT;
import static org.xmlsh.core.XVariable.XVarFlag.READONLY;
import static org.xmlsh.core.XVariable.XVarFlag.UNSET;

import java.util.EnumSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.IMethods;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

public class XVariable {

	private static final String sName = "name";
	private static final String sVariable = "variable";

	private static final String sType = "type";
	private static final String sSimpleType = "simple-type";

	private static final String sFlags = "flags";
	private static final String sTypeFamily = "type-family";



	private static Logger mLogger = LogManager.getLogger();

	public static enum XVarFlag {
		EXPORT , 		// to be exported to child shells
		READONLY,
		UNSET
    ;

  

	};

	/*
	 * Variable Expansion parsed to components
	 * ${ [#|!] varname [ '[' index ']' ] [ ':' expr }  =>
	 * [ prefix , name , index , field ]
	 * 
	 * $x  =>  ( null , x , null , null )
	 * ${!x}  => ( ! , x , null , null )
	 * ${x[foo] => ( null , x , foo , null )
	 * ${x:bar} => (null , x , null , bar )
	 * ${!x[foo]:bar} ( ! , x , foo , bar )
	 */
	
	
	private		String	mName;
	private		XValue	mValue;
	
	private		EnumSet<XVarFlag>	mFlags;
	
	public final static EnumSet<XVarFlag>  XVAR_STANDARD = EnumSet.noneOf(XVarFlag.class);
	 public final static EnumSet<XVarFlag>  XVAR_INIT = EnumSet.of( UNSET );
   public final static EnumSet<XVarFlag>  XVAR_SYSTEM = EnumSet.of( EXPORT );

  
	protected XVariable( String name , XValue value)
	{
		mName = name ;
		mValue = value;
		mFlags = XVAR_STANDARD;
	}
	
	 protected XVariable( String name , XValue value, EnumSet<XVarFlag> flags )
	  {
	    mName = name ;
	    mValue = value;
	    mFlags = flags;
	  }
	
	
	
	@Override
	public XVariable clone()
	{
		XVariable that = new XVariable(mName, getValue());
		return that ;
	}
	
	
	
	
	// helper for flag tests that requre UNSET to be OFF
	private boolean hasFlags( XVarFlag... flags ) {
	  if( isUnset() )
	    return false ;
	  return Util.setContainsAll(mFlags, flags );
	}
	 private boolean hasAnyFlags( XVarFlag... flags ) {
	    if( isUnset() )
	      return false ;
	    return Util.setContainsAny(mFlags, flags );
	  }
	 // helper for flag tests that requre UNSET to be OFF
  private boolean hasFlag( XVarFlag flag ) {
    if( isUnset() )
      return false ;
    return mFlags.contains(flag);
    
  }
  
  public static EnumSet<XVarFlag> addFlag( EnumSet<XVarFlag> flags  , XVarFlag flag )
  {
    assert( flag != UNSET );
    return Util.withEnumAdded( flags , flag );
  }
  
  public static EnumSet<XVarFlag> standardFlags(  )
  {
    return  XVAR_STANDARD;
  }
	
	 /*
	  *  Flag accessors
	  */
  private boolean isUnset()
  {
    return mFlags.contains(UNSET);
  }
   
  
  public boolean isExport() {
    return  hasFlag( EXPORT );
  }
	
	
    /**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return the raw value
	 */
	public XValue getValue() {
		return mValue;
	}

	/**
	 * @param value the value to set
	 * @throws InvalidArgumentException 
	 *  Flags are presumed to be already set
	 */

  public void setValue(XValue value )
  {
    if( mFlags.contains( READONLY ))
      throw new InvalidArgumentException("Cannot modify readonly variable: " + getName());

    mValue = value;
    
    
  }

  
	
	// Set an indexed value   
	// a[ind] = b 
	public void setIndexedValue(XValue value, String ind ) throws CoreException {
        if( mFlags.contains( READONLY ))
            throw new InvalidArgumentException("Cannot modify readonly variable: " + getName());
        
        assert( ! Util.isBlank(ind));
        
          setValue(getValueMethods().setXValue(  getValue() , ind, value));

	}
  public IMethods getValueMethods()
  {
    return getValue().typeFamilyInstance();
  }
        
        
  private int parsePositionalIndex(String ind)
  {
    return Util.parseInt(ind , 1 ) - 1 ;
  }
	
  /**
	 * @return the flags
	 */
	public EnumSet<XVarFlag> getFlags() {
		return mFlags;
	}


	public void setFlag( XVarFlag flag )
	{
	  mFlags = Util.withEnumAdded(mFlags,flag);
	}
	public void serialize(XMLStreamWriter writer) throws SAXException, XMLStreamException {

		XValue value = this.getValue();
		String flagStr = mFlags.toString();
		

		writer.writeStartElement(sVariable);
		writer.writeAttribute(sName, getName());
		writer.writeAttribute(sTypeFamily, value == null ? "null" : value.typeFamily().name() );

		String type  = "null" ;
		String simpleType = type ;
		/*
		if( bSimple || ! arg.isXdmItem() ){

      String type = 
          bSimple ? 
              arg.typeFamilyInstance().simpleTypeName( arg.asObject() ) :
                arg.typeFamilyInstance().typeName( arg.asObject() );   
           
        w.println( type );
      
    }
*/
		
		
		if( value != null && ! value.isNull() ) {
			Object obj = value.asObject();
			ITypeFamily it = value.typeFamilyInstance();
				type = it.typeName(obj);
				assert( type != null );
				simpleType = it.simpleTypeName(obj);
			}
		writer.writeAttribute(sType,type);
		writer.writeAttribute(sSimpleType,simpleType);
		writer.writeAttribute(sFlags, flagStr );
		writer.writeEndElement();



	}




	public void clear() throws InvalidArgumentException {

		setValue( null );


	}

	public boolean isNull() {
		return getValue() == null ;
	}

	public void shift(int n) {

		if( n <= 0 || getValue() == null )
			return ;

		mValue = getValue().shift( n );


	}


	/*
	 * Get a variable value with an optional index and tie expression
	 */

	public XValue getValue(Shell shell, EvalEnv env ,  String ind, String tie) throws CoreException {

		XValue xv = mValue ;
		if( ! Util.isBlank(ind)  ) {
		  // Special case * @
		  if( Util.isOneOf( ind , "*" , "@" ) )
		    return EvalUtils.getValues( env , mValue );
		    
		       xv =  EvalUtils.getIndexedValue(env , mValue,  ind );
		}
		assert( Util.isEmpty(tie));

		return xv ;

	}

	public int getSize()
	{
		return EvalUtils.getSize( getValue() );
	}



	public void unset() throws InvalidArgumentException 
	{
		clear();
		mFlags = Util.withEnumAdded(mFlags,UNSET );
	}
	
	 public void export() throws InvalidArgumentException 
	  {
	    clear();
	    mFlags = Util.withEnumAdded(mFlags,UNSET );
	  }

	 

	public XVariable newValue(XValue value)
  {
    XVariable that = new XVariable(mName, value,mFlags);
    return that ;
  }
	
  public static XVariable newInstance(String name)
  {
    return new XVariable(name, null);
  }
  
  public static  XVariable newInstance(String name , XValue value )
  {
    return new XVariable(name, value);
  }

  public static XVariable newInstance(String name, XValue value, EnumSet<XVarFlag> flags)
  {
    return new XVariable(name, value,flags);

  }
  
  public static  XVariable newInstance(String name , String value )
  {
    return new XVariable(name, XValue.newInstance(value));
    
  }

  public static  <T extends Object> XVariable newInstance(String name , T value )
  {
    return new XVariable(name, XValue.newInstance(value));
  }

  public static  XVariable anonymousInstance(TypeFamily type ) {
    return new XVariable(null,XValue.nullValue(type));

  }

  public static <T extends XValue> XVariable anonymousInstance(T value )
  {
     return new XVariable(null,value);
  }
  
  public static <T extends Object> XVariable anonymousInstance(T value )
  {
     return new XVariable(null, XValue.newInstance(value));
  }

  public static EnumSet<XVarFlag> systemFlags()
  {
    return XVAR_SYSTEM ;
  }

}


//
//
//Copyright (C) 2008-2014    David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//