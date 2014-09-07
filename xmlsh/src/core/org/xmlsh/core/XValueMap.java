/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.util.Util;
import org.xmlsh.util.XNamedValue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/*
 * Generic Properties 
 * A set of Name/Value pairs to any object 
 */
public class XValueMap extends AbstractMap<String,XValue> implements IXValueMap<XValueMap>  {
	private  	Map<String,XValue>   mMap;

	public XValueMap(XValueMap that) {
		mMap = new HashMap<>(that.mMap );
	}

	public XValueMap() {
	       mMap = new HashMap<>();
    }

    public XValueMap(int extent)
  {
      mMap = new HashMap<>(extent);
      
  }

  @Override
	public XValue get( String name ) {
		return mMap.get(name);
	}

	// How many entries
	@Override
	public int size() { return mMap.size() ; }

	@Override
	public boolean isEmpty() { return mMap.isEmpty() ; }

	@JsonAnyGetter 
	public Map<String,XValue> properties(){ return mMap ; }



	@Override
	public void removeAll() {
		mMap.clear();

	}

	@Override
	public XValue put(String key, XValue value)
	{
	  assert( key != null );
	  assert( value != null );
		return  mMap.put(key, value);

	}

	/* (non-Javadoc)
   * @see org.xmlsh.core.IXValueMap#keySet()
   */
  @Override
	public Set<String> keySet()
	{
		return mMap.keySet();
	}

	@Override
	public Collection<XValue> values()
	{
		return mMap.values();
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
		}

	}


	public void addAll( XValueMap map )
	{
		mMap.putAll(map.mMap);
	}

	public void put(String key, Object value)
	{
		if( value instanceof XValue )
			put( key , (XValue) value );
		else
			put( key , XValue.newXValue(null,value) );

	}

	public void put( XValueProperty prop ) {
	    put( prop.getName() , prop.getValue() );
	}
	
	
	
	@Override
	public Set<java.util.Map.Entry<String, XValue>> entrySet()
	{
		return mMap.entrySet();
	}

	// Adds an XValue if it can turn it into a property or property set
	public boolean add(XValue arg)
	{
		
	    if( arg.isInstanceOf( XValueMap.class ))
	        addAll( ((XValueMap) arg.asObject()) );
	    else
	    if( arg.isInstanceOf( XValueProperty.class ))
	        add( ((XValueProperty) arg.asObject()) );

	    else 
	      // Maybe too much hack 
    	    try {
    			  put( XTypeUtils.newNamedValue( arg ) );
    		} catch (InvalidArgumentException e) {
    			Util.wrapException(e,IllegalArgumentException.class);
    		}
		return true ;
	}

	public void put(XNamedValue nv ) 
	{
	    if( Util.isBlank(nv.getName()))
	        throw new IllegalArgumentException( "Cannot add an entry to a map with an empty key");
		put( nv.getName() , nv.getValue() );
	}

	@Override
    public boolean isMap()
    {
	    return true;
    }

	@Override
    public boolean isList()
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean isAtomic()
    {
	    // TODO Auto-generated method stub
	    return false;
    }

    @Override
    public XValue append(XValue item) {
        XValueMap newMap = new XValueMap(this);
        newMap.add(item);
        return XValue.newXValue(newMap);
    }

    public void add(XValueProperty prop) {
       put( prop.getName() , prop.getValue() );
        
    }

    @Override
    public Iterator<XValue> iterator() {
        return mMap.values().iterator();
    }


    @Override
    public XValue asXValue()
    {
      return XValue.newXValue( TypeFamily.XTYPE , this );
    }

    @Override
    public boolean isContainer()
    {
      // TODO Auto-generated method stub
      return true;
    }

    @Override
    public boolean isSequence()
    {
      // TODO Auto-generated method stub
      return false;
    }


    @Override
    public IXValueContainer<? extends IXValueContainer<?>> asXContainer()
    {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public IXValueMap<? extends IXValueMap<?>> asXMap()
    {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public IXValueList<? extends IXValueList<?>> asXList()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
    {
      // TODO Auto-generated method stub
      return null;
    }

    public void put(java.util.Map.Entry<String, XValue> e)
    {
      put( e.getKey() , e.getValue() );
      
    }

	@Override
	public boolean containsKey(String key) {
		return mMap.containsKey(key);
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