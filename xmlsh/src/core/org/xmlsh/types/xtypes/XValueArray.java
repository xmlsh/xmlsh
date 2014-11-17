package org.xmlsh.types.xtypes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

/*
 * A sparse array indexable by string or integer
 * Default starts at index 1 (not 0)
 * 
 */
public class XValueArray extends AbstractList<XValue> implements IXValueMap, IXValueList 
{
	private static XValueArray _emptyArray = new XValueArray();
  private     TreeMap<Integer,XValue>   mArray;
	private     int maxIndex = -1; // maximum integer value
	public XValueArray() {
		mArray = new TreeMap<>();
	}
	
    public XValueArray( XValueArray that ) {
        mArray = new TreeMap<>( that.mArray );
    }

	public int extent() { return maxIndex ; }
	@Override
	public int size() { return mArray.size() ; }
	@Override
	public boolean isEmpty() { return mArray.isEmpty() ; }





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
	public boolean add( XValue arg )
	{
		mArray.put( Integer.valueOf(++maxIndex), arg);
		return true ;
	}

	@Override
	public Set<String> keySet()
	{
		TreeSet<String> s = new TreeSet<String>(
				new Comparator<String>() {

					@Override
					public int compare(String o1, String o2)
					{
						return  Integer.valueOf( o1).compareTo
								(Integer.valueOf(o2));
					}
				});
		for( Integer i : mArray.keySet() )
			s.add( i.toString());
		return s;
	}

	@Override
	public Collection<XValue> values()
	{

		return mArray.values();
	}

	public void addAll(List<XValue> args)
	{

		for( XValue a : args )
			add(a);

	}

	@Override
	public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException
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
		}

	}

	@Override
	public XValue get(int index)
	{
		return mArray.get(Integer.valueOf(index));
	}

	@Override
    public boolean isMap()
    {
	    return false;
    }

	@Override
    public boolean isList()
    {
	    return true;
    }

	@Override
    public boolean isAtomic()
    {
	    return false;
    }

	/* 
	 * Create a new value by appending this one
	 * @see org.xmlsh.core.IXValueContainer#append(org.xmlsh.core.XValue)
	 */
    @Override
    public XValue append(XValue item) throws InvalidArgumentException {
        
        XValueArray  newArray = new XValueArray(this);
        newArray.add( item );
        return XValue.newXValue( TypeFamily.XTYPE , newArray ) ;
        
        
    }

    @Override
    public XValue getAt(int index)
    {
      return get(index);
    }

    @Override
    public XValue setAt(int index, XValue value)
    {
      return  mArray.put(index , value);
    }

    @Override
    public XValue asXValue() throws InvalidArgumentException
    {
      return XValue.newXValue( TypeFamily.XTYPE , this );
    }

    @Override
    public boolean isContainer()
    {
      return true ;
    }

    @Override
    public boolean isSequence()
    {
     return false ;
    }


    @Override
    public IXValueContainer asXContainer()
    {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public IXValueMap asXMap()
    {
      return this;
    }

    @Override
    public IXValueList asXList()
    {
      return this;
    }

    @Override
    public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
    {
      return new XValueSequence( this );
    }

    public static Object emptyArray()
    {
      return _emptyArray;
    }

    @Override
    public List<XValue> asList()
    {
      return Util.toList(iterator());
    }

	@Override
	public XValue get(String name) {
      return getAt( Util.parseInt(name, 0));
	
	}

	@Override
	public XValue put(String key, XValue value) {
		return setAt( Util.parseInt(key,0),value);
		
	}

	@Override
	public boolean containsKey(String key) {
		return mArray.containsKey(Util.parseInt(key,0));
	}


}
