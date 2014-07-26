package org.xmlsh.core;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xmlsh.util.Util;

/*
 * A sparse array indexable by string or integer
 * Default starts at index 1 (not 0)
 * 
 */
public class XValueArray extends AbstractList<XValue>  implements XValueContainer<XValueArray> 
{
    private     SortedMap<Integer,XValue>   mArray;
    private     int extent = 0; // maximum integer value
    public XValueArray() {
        mArray = new TreeMap<>();
    }
    
    // Public position getter is 0 based
    @Override 
    public XValue get( int  pos ) {
        if( pos > extent )
            return null ;
        return mArray.get(Integer.valueOf(pos));
    }
    public int extent() { return extent ; }
    public int size() { return mArray.size() ; }
    public boolean isEmpty() { return mArray.isEmpty() ; }
    
    
    public void setAt( int pos , XValue value ) {
        if( pos >= extent )
            extent = pos ;
        mArray.put( Integer.valueOf(pos), value);
    }

    
    

    @Override
    public XValue get(String name) {
        int ind = Util.parseInt(name, -1);
        if( ind < 0 )
            return null ;
            
        return get( ind );
    }


    @Override
    public XValueArray removeAll() {
        mArray.clear();
        extent = 0;
        return this;
        
    }
    
/*    
 * Different then setAt as it should shift everyting - not supported

    @Override
    public void add(int index, XValue element) {
        
    }
*/
    
    @Override
    public boolean add( XValue arg )
    {
       mArray.put( Integer.valueOf(++extent), arg);
       return true ;
    }
    public void addAll(List<XValue> args) {
        for(XValue arg : args )
            add(arg);
    }


}
