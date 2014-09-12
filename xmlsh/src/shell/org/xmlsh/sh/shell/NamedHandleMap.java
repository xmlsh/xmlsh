package org.xmlsh.sh.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlsh.core.IHandle;
import org.xmlsh.types.ITypeConverter;
import org.xmlsh.util.TypeConvertingIterator;
import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class NamedHandleMap<V,T extends IHandle<V>>  extends HashMap<String, T> 
{
	 List<V> valueList() { 
		    return Util.toList( valueIterator() );
		    
		  }
	 
	 public Iterable<V> valueIterable() {
		    return new Iterable<V>(){

		      @Override
		      public Iterator<V> iterator()
		      {
		        return valueIterator();
		      }};
		    
		  }
		  
		  public Iterator<V> valueIterator()
		  {
		    ITypeConverter<T, V> converter = new ITypeConverter<T,V>(){

		      @Override
		      public V convert(T handle)
		      {
		       return handle.get();
		      }};
		      
		    return new TypeConvertingIterator<T,V>( super.values().iterator() , converter);
		   
		  }
		  
	     public boolean contains( V v )
	     {
	    	 for( T handle : values() ){
	    		 if( handle.get().equals(v))
	    			 return true ;
	    	 }
	    	 return false ;
	     }

		  

}
