/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.types.properties;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xmlsh.annotations.Container;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.IXValueMap;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

@Container
public class propertyFunctions 
{
	@Function( name="keys")
	public static class keys extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			 XValueSequence list = new XValueSequence();
			    for( XValue x : args ) { 
			      if( x.isInstanceOf(XValueProperties.class))
			          for( String keys : Util.toList(  
			        		    ((XValueProperties)x.asInstanceOf(XValueProperties.class)).keySet().iterator()) ) 
			        		{
			            list.addValue( XValue.newXValue(keys) );
			          }
			        }
			    return list.asXValue();
		}
	}
	
	@Function( name="values")
	public static class values extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			 XValueSequence list = new XValueSequence();
			    for( XValue x : args ) { 
			      if( x.isInstanceOf(XValueProperties.class))
			          for( XValue xv  :  ((XValueProperties)x.asInstanceOf(XValueProperties.class)).values() )
			            list.addValue( xv );
			          }
			    return list.asXValue();
		}
	}
	
	@Function( name="get")
	public static class get extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 2 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key");
				return null;
				
			}
			return  args.get(0).asInstanceOf(XValueProperties.class).get( args.get(1).toString() );
	}
	}
	
	@Function( name="put")
	public static class set extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 3 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key");
				return null;
			}
			return  args.get(0).asInstanceOf(XValueProperties.class).put( args.get(1).toString() , args.get(2) );
	}
	}

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

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
 * 
 */