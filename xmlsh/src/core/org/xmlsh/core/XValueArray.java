package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/*
 * A sparse array indexable by string or integer
 * Default starts at index 1 (not 0)
 * 
 */
public class XValueArray   implements XValueContainer<XValueArray> 
{
    private     TreeMap<Integer,XValue>   mArray;
    private     int maxIndex = -1; // maximum integer value
    public XValueArray() {
        mArray = new TreeMap<>();
    }
    
    public int extent() { return maxIndex ; }
    public int size() { return mArray.size() ; }
    public boolean isEmpty() { return mArray.isEmpty() ; }
    
    
    

    @Override
    public XValue get(String name) {
        int ind = Util.parseInt(name, -1);
        if( ind < 0 || ind > maxIndex  )
            throw new ArrayIndexOutOfBoundsException() ;
            
        return mArray.get(Integer.valueOf(ind));
    }


    @Override
    public void removeAll() {
        mArray.clear();
        maxIndex = -1;
        
    }
    
/*    
 * Different then setAt as it should shift everyting - not supported

    @Override
    public void add(int index, XValue element) {
        
    }
*/
    
    @Override
    public void add( XValue arg )
    {
       mArray.put( Integer.valueOf(++maxIndex), arg);
    }

	@Override
    public XValue put(String key, XValue value)
    {
        int ind = Util.parseInt(key, -1);
        if( ind < 0 )
            throw new ArrayIndexOutOfBoundsException() ;

       return  mArray.put(Integer.valueOf(ind), value);
    }

	@Override
    public Iterator<String> keyIterator()
    {
	   return Util.stringIterator( mArray.navigableKeySet().iterator() );
    }

	@Override
    public Iterator<XValue> valueIterator()
    {

		return mArray.values().iterator();
    }

	public void addAll(List<XValue> args)
    {
	    
		for( XValue a : args )
			add(a);
	    
    }

	@Override
    public void serialize(OutputStream out, SerializeOpts opts) throws IOException
    {
	   try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){
		   String sep = "";

		   ps.write("[");
		   for(  Entry<Integer, XValue> entry : mArray.entrySet() ) {
			   ps.write(sep);;
			    ps.write( "[" );
			    ps.write( entry.getKey().toString());
			    ps.write("]=");
			    ps.flush();
			    entry.getValue().serialize(out, opts);
			    ps.write(" ");
			    sep = ",";
		   }
		   ps.write("]");
	   } catch (InvalidArgumentException e) {
         Util.wrapIOException(e);
    }
	    
    }


}
