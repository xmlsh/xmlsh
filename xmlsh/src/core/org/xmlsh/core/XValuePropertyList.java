/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * A list of objects indexable by string or index (1 based for strings)
 */
public class XValuePropertyList  implements IXValueContainer<XValuePropertyList> 
{
	private  	List<XValueProperty>   mList;

	public XValuePropertyList(XValueProperty p) {
		mList = new LinkedList<>();
		mList.add(p);
	}

	public XValuePropertyList(XValuePropertyList that) {
	    mList = new LinkedList<>( that.mList );
    }

	public XValuePropertyList()
  {
	   mList = new LinkedList<>();

  }

  public boolean add( XValueProperty value ) {
		return mList.add(value);
	}
	 public boolean add( XValue value ) {
	    return mList.add( XValueProperty.instanceOf(value) );
	  }
	 

	public XValue getAt( int pos) {
		if( pos < 0 || pos > mList.size() )
			return null ;
		return mList.get(pos).getValue();
	}

	public int count() { return mList.size() ; }

	@Override
	public boolean isEmpty() { return mList.isEmpty() ; }

	@Override
	public int size() {
		return mList.size();
	}


	// Get like a map - returns a sequence of XValues
	@Override
	public XValue get(String name) {
		
		XValueList list  = null ;
		for( XValueProperty p : mList ) {
		  if( p.nameEquals(name) ) {
		    if( list == null )
		      list = new XValueList();
		    list.add( p.getValue() );
		  }
		}
		if( list == null )
		  return new XValue(TypeFamily.XTYPE,null);
		if( list.size() == 1 )
		  return list.get(0);
		return new XValue( list );
		
	}

	@Override
	public void removeAll() {
		mList.clear();

	}

	@Override
	public XValue put(String key, XValue value)
	{
		mList.add( new XValueProperty( key , value ) );
    return null ; 
	}

	
	// Sorted set in order of key listings but unique
	@Override
	public Set<String> keySet()
	{
	  // Get a list of all non duplicate keys in order of entry 
	  final ArrayList<String> keys = new ArrayList<>();
	  for( XValueProperty p : mList ) {
	    if( ! keys.contains(p.getName()))
	      keys.add(p.getName());
	  }
	  
	  return new AbstractSet<String>() {

      @Override
      public Iterator<String> iterator()
      {
        return keys.iterator();
      }

      @Override
      public int size()
      {
        return keys.size();
      }
	  };
 	      
	  
	}


	@Override
	public void serialize(OutputStream out, SerializeOpts opts) throws IOException
	{
		try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){
			ps.write("[");
			String sep = "";
			for( XValueProperty prop : mList  ) {
				ps.write( sep );
				ps.flush();
				prop.serialize(out, opts);
				ps.write(" ");
				sep = ",";
			}
			ps.write("]");
		} 

	}


	@Override
    public boolean isMap()
    {
	    return true;
    }

	@Override
    public boolean isList()
    {
	    return true;
    }

	@Override
    public boolean isAtomic()
    {
	    return false ;
    }

    @Override
    public XValue append(XValue item) {
        XValuePropertyList newList = new XValuePropertyList(this);
        newList.add( XValueProperty.instanceOf( item ) );
        return new XValue(  newList ); 
    }

    @Override
    public Collection<XValue> values()
    {
      
     return  Util.toList( iterator() );
    }

    @Override
    public Iterator<XValue> iterator()
    {
      
        return new Iterator<XValue>() {

          Iterator<XValueProperty>  iter = mList.iterator();

          @Override
          public boolean hasNext()
          {
            return iter.hasNext();
          }

          @Override
          public XValue next()
          {
            return iter.next().getValue();
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException();
            
          }
        };
      
      }

    @Override
    public XValue setAt(int index, XValue value)
    {
      mList.set( index ,XValueProperty.instanceOf( value ) );
      return null ; 

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