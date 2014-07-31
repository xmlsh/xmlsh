/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
    public void removeAll() {
        mMap.clear();
        
    }

	@Override
    public void add(XValue value)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public XValue put(String key, XValue value)
    {
		return  mMap.put(key, value);
		 
    }

	@Override
    public Iterator<String> keyIterator()
    {

		return mMap.keySet().iterator();
    }

	@Override
    public Iterator<XValue> valueIterator()
    {
	    return mMap.values().iterator();
    }
	
	@Override
    public void serialize(OutputStream out, SerializeOpts opts) throws IOException
    {
	 try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){
		   String sep = "";

		   ps.write("{");
		   for(  Entry<String, XValue> entry : mMap.entrySet() ) {
			   ps.write(sep);;
			    ps.write( "[" );
			    ps.write( entry.getKey().toString());
			    ps.write("]=");
			    ps.flush();
			    entry.getValue().serialize(out, opts);
			    ps.write(" ");
			    sep = ",";
		   }
		   ps.write("}");
	   } catch (InvalidArgumentException e) {
       Util.wrapIOException(e);
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