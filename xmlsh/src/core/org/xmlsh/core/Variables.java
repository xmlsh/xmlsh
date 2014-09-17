/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.xmlsh.util.NameValueMap;

@SuppressWarnings("serial")
public class Variables {
	private		NameValueMap<XVariable> mGlobals;
	private		NameValueMap<XVariable> mLocals = new NameValueMap<XVariable>();


	public Variables() 
	{
		mGlobals = new NameValueMap<XVariable>();

	}
	Variables(Variables that) {
		mGlobals = that.mGlobals.clone();
		mGlobals.putAll( that.mLocals.clone() );
	}
	private Variables( NameValueMap<XVariable> globals , NameValueMap<XVariable>  locals )
	{
		mGlobals = globals ; 
		mLocals = locals ;
	}
	

	public XVariable get(String name) {
		// First look in locals
	  XVariable v = mLocals.get(name);
	  if( v == null )
	    v = mGlobals.get(name);
	  return  v;

	}

	   
	public void put(XVariable var) {
		String name = var.getName();
    if( mLocals.containsKey(name)  || var.isLocal() ) {
			mLocals.put(name , var  );
		}
		else
			mGlobals.put(name,var);
	}
	public Collection<String> getVarNames() {
		Set<String> names = new HashSet<String>(mGlobals.size() + mLocals.size());

		names.addAll( mGlobals.keySet() );
		names.addAll( mLocals.keySet() );
		return names ;


	}
	public void unset(String name) throws InvalidArgumentException 
	{
		// Unsetting a local just sets its value to null
		XVariable var = mLocals.get(name);
		if( var != null )
			var.unset();
		else
		{
			var = mGlobals.get(name);
			if( var != null){
			  var.unset();
			  mGlobals.remove(name);
			}
		}
			



	}
	public boolean containsKey(String name) {
		return mGlobals.containsKey(name) || mLocals.containsKey(name);
	}
	/* 
	 * Create a copy of the Variables where the globals are identical but the locals are cloned
	 * 
	 */
	public Variables pushLocals() {
		NameValueMap<XVariable> locals = new NameValueMap<XVariable>();
		locals.putAll( mLocals );
		return new Variables( mGlobals ,  locals );
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
