/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xmlsh.util.Util;

/*
 * A list of objects indexable by string or index (1 based for strings)
 */
public class XValueList  extends AbstractList<XValue>  implements XValueContainer<XValueList> 
{
	private  	List<XValue>   mList;
	
	public XValueList() {
	    mList = new LinkedList<>();
	}

	public boolean add( XValue value ) {
	   return mList.add(value);
	}

	public XValue getAt( int pos) {
		if( pos < 0 || pos > mList.size() )
		    return null ;
		return mList.get(pos);
	}
	
	public int count() { return mList.size() ; }
	
	@Override
	public boolean isEmpty() { return mList.isEmpty() ; }

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public XValue get(int index) {
        return getAt(index);
    }

    // convert string to 1 basd index 
    @Override
    public XValue get(String name) {
        int ind = Util.parseInt(name, 0);
        if( ind <= 0 )
            return null ;
        return get(ind-1 );
    }

    @Override
    public XValueList removeAll() {
      mList.clear();
      return this;
        
    }

    @Override
    public boolean addAll(Collection<? extends XValue> args) {
        return mList.addAll(args);
    }

    @Override
    public Iterator<XValue> iterator() {
       return mList.iterator();
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