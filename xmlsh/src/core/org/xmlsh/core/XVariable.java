/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xml.sax.SAXException;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.IMethods;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

import java.util.EnumSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static org.xmlsh.core.XVariable.XVarFlag.*;

public class XVariable {

	private static final String sName = "name";
	private static final String sVariable = "variable";
	private static final String sKind = "kind";

	private static final String sType = "type";
	private static final String sSimpleType = "simple-type";

	private static final String sFlags = "flags";
	private static final String sTypeFamily = "type-family";



	private static Logger mLogger = LogManager.getLogger(XVariable.class);

	public static enum XVarFlag {
		EXPORT , 		// to be exported to child shells
		XEXPR ,			// participates in XEXPRs
		READONLY,
		LOCAL,
		UNSET,
		
		
		POSVAR,     // special $0..n or $* / $@
		SEQUENCE,    // secuence var
		LIST ,      // List value (Explicit list of XVars like $@
		NAMED_INDEX  ,   // Map or object - named fields
		POSITIONAL_INDEX   // Positional index
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
	

	
	private		XQueryExecutable		mTieExpr;	// Tie expression
	public final static EnumSet<XVarFlag>  XVAR_STANDARD = EnumSet.of( EXPORT , XEXPR );
	public final static EnumSet<XVarFlag>  XVAR_STANDARD_LOCAL = EnumSet.of( EXPORT , XEXPR , LOCAL );
	public final static EnumSet<XVarFlag>  XVAR_SHELLARG = EnumSet.of( EXPORT , XEXPR , POSITIONAL_INDEX );
  public final static EnumSet<XVarFlag>  XVAR_SHELLARG_LOCAL = EnumSet.of( EXPORT , XEXPR , POSITIONAL_INDEX, LOCAL  );
  public final static EnumSet<XVarFlag>  XVAR_SEQUENCE = EnumSet.of( POSITIONAL_INDEX , SEQUENCE  );
 
  
  public final static EnumSet<XVarFlag>  XVAR_TYPEMASK = EnumSet.of( POSVAR , POSITIONAL_INDEX , SEQUENCE , LIST   , NAMED_INDEX);
  public final static EnumSet<XVarFlag>  XVAR_PRIVMASK = EnumSet.of( EXPORT ,   XEXPR ,   READONLY,  LOCAL,  UNSET ); 
  
	public XVariable( String name , XValue value , EnumSet<XVarFlag> flags)
	{
		mName = name ;
		mValue = value;
		mFlags = flags;

	}
	@Override
	public XVariable clone()
	{
		XVariable that = new XVariable(mName,getValue(),mFlags);
		that.mTieExpr = mTieExpr ;
		return that ;
	}
	
	
	
	
	@Deprecated
	public XVariable( String name , XValue value )
	{
		this( name , value ,XVAR_STANDARD );

	}

	protected XVariable( String name , EnumSet<XVarFlag> flags )
	{
		this( name , null , flags );
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
    return Util.withEnumAdded( flags , flag );
  }
  
  
  public static EnumSet<XVarFlag> standardFlags( XVarFlag...flags )
  {
    return Util.withEnumsAdded( XVAR_STANDARD, flags);
  }
  public static EnumSet<XVarFlag> standardFlags( EnumSet<XVarFlag> flags )
  {
    return Util.withEnumsAdded( XVAR_STANDARD, flags);
  }
  public static EnumSet<XVarFlag> shellArgFlags(XVarFlag...flags ){
    return Util.withEnumsAdded( XVAR_SHELLARG, flags);
  }

  public static EnumSet<XVarFlag> shellArgFlags(EnumSet<XVarFlag> flags){
    return Util.withEnumsAdded( XVAR_SHELLARG, flags);
  }
 
  public static Enum<XVarFlag> localFlag() {
    return LOCAL ;
  }
  
  public static EnumSet<XVarFlag> listFlags()
  {
    return EnumSet.of( LIST ,POSVAR ,  POSITIONAL_INDEX );
    
  }
    
	
	 /*
	  *  Flag accessors
	  */
  private boolean isUnset()
  {
    return mFlags.contains(UNSET);
  }
   
  
  public   boolean isIndexed() {
   return hasAnyFlags( NAMED_INDEX , POSITIONAL_INDEX );
  }
  public  boolean isNameIndexed() {
    return hasFlag(  NAMED_INDEX );
  }
  public  boolean isPositionIndexed() {
    return hasFlag(  POSITIONAL_INDEX );
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
	public void setValue(XValue value) throws InvalidArgumentException {
	
	  setValue( value , value == null ? XVAR_STANDARD : value.typeFlags() );
	}

  public void setValue(XValue value, EnumSet<XVarFlag> varFlags)
  {
    if( mFlags.contains( READONLY ))
      throw new InvalidArgumentException("Cannot modify readonly variable: " + getName());

    EnumSet<XVarFlag> old = Util.withEnumsMasked( mFlags , XVAR_PRIVMASK );
    
    mFlags = Util.withEnumsAdded(old, 
        Util.withEnumsMasked( varFlags, XVAR_TYPEMASK ) );
    mValue = value;
    
    
  }

  
	
	// Set an indexed value   
	// a[ind] = b 
	public void setIndexedValue(XValue value, String ind ) throws CoreException {
        if( mFlags.contains( READONLY ))
            throw new InvalidArgumentException("Cannot modify readonly variable: " + getName());
        
        if( ! isIndexed() ) 
          throw new InvalidArgumentException("Cannot set non indexed variable by index: " + getName());
        
        assert( ! Util.isBlank(ind));
        
        if( isNameIndexed() )
          setValue(getValueMethods().setXValue(  getValue() , ind, value));
        else
          setValue(getValueMethods().setXValue(  getValue() , parsePositionalIndex(ind), value));

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
		mFlags.add(flag);
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

	public void tie(Shell shell , String expr) throws SaxonApiException {

		if( expr == null )
		{
			mTieExpr = null ;
			return ;

		}


		Processor processor = Shell.getProcessor();

		XQueryCompiler compiler = processor.newXQueryCompiler();

		// Declare the extension function namespace
		// This can be overridden by user declarations
		compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);

		NameValueMap<String> ns = shell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);

			}

		}


		StringBuffer sb = new StringBuffer();

		sb.append("declare variable $_ external;\n");
		sb.append(expr);

		mTieExpr = compiler.compile( sb.toString() );




	}

	private  XValue	 getTiedValue( Shell shell , XdmItem  item  , String tie) throws CoreException
	{


		Shell saved_shell = ShellContext.set(shell);

		XQueryEvaluator eval = mTieExpr.load();


		try {

			//		eval.setExternalVariable( new QName("_") , new XValue( TypeFamily.XDM , tie ).asXdmValue() );
			
		  eval.setExternalVariable( new QName("_") , new XValue(  tie ).toXdmItem() );

			eval.setContextItem(item);

			XdmValue result =  eval.evaluate();


			return new XValue(result) ;


		} catch (SaxonApiException e) {
			String msg = "Error expanding xml expression: " + tie ;
			mLogger.warn( msg , e );
			throw new CoreException(msg  , e );

		}
		finally {
			ShellContext.set(saved_shell);

		}



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
		    
		  
		  if( ! isIndexed() )
		    shell.printErr("Attempting to get indexed value from non indexed variable: " + getName() );
		  else
		    if( isNameIndexed()  )
		       xv =  EvalUtils.getIndexedValue(env , mValue,  ind );
		    else
          xv =  EvalUtils.getIndexedValue(env , mValue, parsePositionalIndex(ind) );

		}

		if( tie != null && xv.isXdmItem()  )
			xv = getTiedValue(shell, xv.asXdmItem() , tie );
		return xv ;

	}

	public int getSize()
	{
		return EvalUtils.getSize( getValue() );



	}



	public void unset() throws InvalidArgumentException 
	{
		clear();
		mFlags.add( UNSET );
	}
  public static EnumSet<XVarFlag> shellArgListFlags()
  {
    return Util.withEnumsAdded(XVAR_SHELLARG, listFlags());
  }
  
  /*
   * Like clone, will preserve tied value
   * but will augment flags
   */
  public XVariable newValue(XValue value, EnumSet<XVarFlag> flags)
  {
    
    EnumSet<XVarFlag> newFlags = Util.withEnumsMasked( mFlags, 
      EnumSet.of(XVarFlag.LOCAL , XVarFlag.READONLY, XVarFlag.EXPORT)  );
    
    newFlags = Util.withEnumsAdded( flags );
    
    XVariable that = new XVariable(mName,value,newFlags);
    that.mTieExpr = mTieExpr ;
    return that ;
    
     
    
  }
  public static EnumSet<XVarFlag> sequenceFlags()
  {
    return XVAR_SEQUENCE ;
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