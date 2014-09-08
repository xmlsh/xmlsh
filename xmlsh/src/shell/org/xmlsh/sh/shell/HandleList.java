package org.xmlsh.sh.shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlsh.core.IHandle;
import org.xmlsh.types.ITypeConverter;
import org.xmlsh.util.TypeConvertingIterator;
import org.xmlsh.util.Util;

@SuppressWarnings("serial") class HandleList<V,T extends IHandle<V>>  extends ArrayList< T  > 
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
      
    return new TypeConvertingIterator<T,V>(iterator(), converter);
   
  }
  
  public boolean containsValue( V v ){
    for( T hv : this ){
      if( v == hv || v.equals(hv))
        return true ;
      
    }
    return false ;
  }

  
}