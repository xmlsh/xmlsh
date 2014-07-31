/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/*
 * A list of objects indexable by string or index (1 based for strings)
 */
public class XValueList  implements XValueContainer<XValueList> 
{
	private  	List<XValue>   mList;
	
	public XValueList() {
	    mList = new LinkedList<>();
	}

	public void add( XValue value ) {
	   mList.add(value);
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


    // convert string to 0 basd index 
    @Override
    public XValue get(String name) {
        int ind = Util.parseInt(name, 0);
        if( ind <= 0 || ind >= size() )
            return null ;
        return mList.get(ind);
    }

    @Override
    public void removeAll() {
    	mList.clear();
        
    }

	@Override
    public XValue put(String key, XValue value)
    {
		 int ind = Util.parseInt(key, 0);
	     if( ind <= 0 || ind >= size() )
	          throw new ArrayIndexOutOfBoundsException();
	     return mList.set(ind, value);
	      
    }

	@Override
    public Iterator<String> keyIterator()
    {

		return Util.rangeIterator(0,size());
		
    }

	@Override
    public Iterator<XValue> valueIterator()
    {
	    return mList.iterator();
    }

	public void addAll(List<XValue> args)
    {
	    mList.addAll(args);
    }

	@Override
    public void serialize(OutputStream out, SerializeOpts opts) throws IOException
    {
	   try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){
		   ps.write("[");
		   String sep = "";
		   for( XValue value : mList  ) {
			    ps.write( sep );
			    ps.flush();
			    value.serialize(out, opts);
			    ps.write(" ");
			    sep = ",";
		   }
		   ps.write("]");
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