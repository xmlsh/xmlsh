/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlsh.util.Util;



public class Options
{
	
	/*
	 * Option defines an option by name, for readability including the "-"
	 * e.g "-a" or "-help"
	 * 
	 */
	
	
	public static class	OptionDef
	{
		String 		flag;
		boolean		hasArgs;
		OptionDef() { flag = null ; hasArgs = true ; }
		public OptionDef( String flag , boolean arg ){
			this.flag = flag;
			this.hasArgs = arg;
		}
	}
	
	
	
	
	public static class	OptionValue
	{
		OptionDef		option;
		XValue	 		value;
		/**
		 * @return the option
		 */
		public OptionDef getOption() {
			return option;
		}
		/**
		 * @return the arg
		 */
		public XValue getValue() {
			return value;
		}
		
		
	}
	
	private List<OptionDef> mDefs;
	private List<XValue> mArgs;
	private List<XValue> mRemainingArgs;
	private List<OptionValue> mOptions;
	
	
	/*
	 * Parse a string list shorthand for options defs
	 * "a,b:,cde:" =>  ("a",false),("b",true),("cde",true)
	 */
	
	
	
	public static List<OptionDef> parseDefs(String sdefs)
	{
		ArrayList<OptionDef>	defs = new ArrayList<OptionDef>();
		
		String[] adefs = sdefs.split(",");
		for( String sdef : adefs ){
			boolean bHasArgs = false ;
			if( sdef.endsWith(":")){
				sdef = sdef.substring(0,sdef.length()-1);
				bHasArgs = true ;
			}
			defs.add( new OptionDef(sdef , bHasArgs));
			
		}
		
		return defs;
		
	}
	
	
	public Options( String  options ,  List<XValue> args )
	{
		this( parseDefs(options) , args);
	}
	
	
	private Options( List<OptionDef>  options ,  List<XValue> args )
	{
		mDefs = options;
		mArgs = args;
		
	}
	

	private OptionDef	getOptDef(String str)
	{
		
		if( mDefs == null )
			return null;
		
		for (OptionDef opt : mDefs) {

			if( Util.isEqual( str , opt.flag ) )
				return opt;
			
		}
		return null;
	}
	
	
	public List<OptionValue>	parse() throws UnknownOption
	{
		if( mOptions != null )
			return mOptions;
			
		
		
		mOptions = new ArrayList<OptionValue>();
		
		
		for ( Iterator<XValue> I = mArgs.iterator() ; I.hasNext() ; ) {
			XValue arg = I.next();
			
			if( arg.isString() &&  arg.toString().startsWith("-") && ! arg.equals("--")){
				String a = arg.toString().substring(1);
				
				OptionDef def = getOptDef(a);
				if( def == null )
					throw new UnknownOption("Unknown option: " + arg);
				OptionValue ov = new OptionValue();
				ov.option = def ;
				if( def.hasArgs ){
					if( !I.hasNext() )
						throw new UnknownOption("Option has no args: " + arg);
					ov.value = I.next();
				}
				mOptions.add(ov);
				
			} else {

				mRemainingArgs = new ArrayList<XValue>( );
				
				if( arg.isString() && arg.equals("--") )
						arg = null;
				if( arg != null )
					mRemainingArgs.add(arg);
				while( I.hasNext() )
					mRemainingArgs.add( I.next());
				
					
				break;
	
			}
				
		}
		return mOptions;
		
	}
	
	public OptionValue	getOpt( String opt )
	{
		for( OptionValue ov : mOptions ){
			if( ov.getOption().flag.equals(opt))
				return ov;
			
		}
		return null;
	}
	public boolean		hasOpt( String opt )
	{
		return getOpt(opt) != null;
		
	}
	
	public String getOptString( String opt , String defValue )
	{
		if( hasOpt(opt))
			return getOpt(opt).getValue().toString();
		else
			return defValue ;
		
	}
	public List<XValue> getRemainingArgs()
	{
		if( mRemainingArgs == null )
			mRemainingArgs = new ArrayList<XValue>(0);
		return mRemainingArgs;
	}
}

//
//
//Copyright (C) 2008, David A. Lee.
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
