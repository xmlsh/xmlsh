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
public class XValueMap implements XValueContainer<XValueMap>
{
	private  	Map<String,XValue>   mMap;
	
	public XValueMap() {
		mMap = new HashMap<>();
	}

	@JsonAnySetter
	public void set( String name , XValue value ) {
		mMap.put( name, value);
	}

	public XValue get( String name ) {
		return mMap.get(name);
	}
	
	// How many entries
	public int size() { return mMap.size() ; }
	
	public boolean isEmpty() { return mMap.isEmpty() ; }
	
	@JsonAnyGetter 
	public Map<String,XValue> properties(){ return mMap ; }

    @Override
    public XValue get(int index) {
       return get( String.valueOf(index));
    }


    @Override
    public XValueMap removeAll() {
        mMap.clear();
        return this;
        
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