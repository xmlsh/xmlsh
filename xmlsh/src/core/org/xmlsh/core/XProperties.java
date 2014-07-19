/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/*
 * Generic Properties 
 * A set of Name/Value pairs to any object 
 */
public class XProperties
{
	private  	Map<String,Object>   mProperties;
	
	public XProperties() {
		mProperties = new HashMap< String , Object >();
	}

	@JsonAnySetter
	public void set( String name , Object value ) {
		mProperties.put( name, value);
	}

	public Object get( String name ) {
		return mProperties.get(name);
	}
	
	@JsonAnyGetter 
	public Map<String,Object> properties(){ return mProperties ; }

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