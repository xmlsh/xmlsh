package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;


/*
 * A "Sequence" of XValues which is analogous to an XdmValue which is 0 or more XdmItems
 * Sequences are "flat" that is if other sequences are added to them as children the the children are added instead
 * 
 * Since an XValue is a sequence itself then 
 * only lists of > 1 XValues need be implemented
 */
public  class XValueSequence implements Iterable<XValue>  , IXValueContainer<XValueProperty> 
{
  private static XValueSequence _emptySequence = new XValueSequence();

  private List<XValue> mList;
  

  public XValueSequence( XValue... values ){
    mList = new ArrayList<XValue>();
    for(XValue v : values )
      addValue(v);
  }


  
  XValueSequence( List<XValue> values ){
    // Flatten sequence - dont let them stack
    mList = new ArrayList<XValue>();
    for( XValue v : values ) {
      addValue(v);
    }
    
  }
  
  
  public static XValueSequence emptySequence() {
    return _emptySequence;
  }
  

  public void addValue(XValue v)
  {
    for( XValue v2 : v ) {
      assert( ! (v2.asObject() instanceof XValueSequence ));
      mList.add( v2 );
    }
  }
  
  XValueSequence( XValueSequence that ){
   mList = new  ArrayList<XValue>( that.mList );
  }

  
  
  @Override
  public Iterator<XValue> iterator()
  {
   return mList.iterator();
  }


  public int size()
  {
    return mList.size();
  }
  

  public boolean isAtomic()
  {
    return mList.size() == 1  && mList.get(0).isAtomic() ;
  }


  @Override
  public boolean isEmpty()
  {
    return mList.isEmpty();
  }


  @Override
  public boolean isMap()
  {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public boolean isList()
  {
    return true;
  }


  @Override
  public XValue put(String key, XValue value)
  {
    throw new UnsupportedOperationException("put is not implemented for XValueSequence");

  
  }


  @Override
  public XValue get(String name)
  {
    int ind = Util.parseInt(name, 0);
    return getAt(ind);
  }


  @Override
  public XValue getAt(int index)
  {
    if( index < 0 || index >= size() )
      return null ;
    return mList.get(index);
    
  }


  @Override
  public void removeAll()
  {
    throw new UnsupportedOperationException("removeAll is not implemented for XValueSequence");
    
  }


  @Override
  public Set<String> keySet()
  {
    throw new UnsupportedOperationException("keySet is not implemented for XValueSequence");

  }


  @Override
  public Collection<XValue> values()
  {
    return Collections.unmodifiableCollection(mList);
  }


  @Override
  public void serialize(OutputStream out, SerializeOpts opts) throws IOException
  {
    ( new XValueList( mList )).serialize(out, opts);
  }


  @Override
  public XValue append(XValue item)
  {
    XValueSequence s = new XValueSequence(this);
    s.addValue(item);

    return new XValue( TypeFamily.XTYPE , s );
    
  }

  @Override
  public XValue setAt(int index, XValue value)
  {
    throw new UnsupportedOperationException("setAt is not implemented for XValueSequence");

  }
  
  
  @Override
  public String toString() {
    // TODO: Shouldnt be called !
    if( mList.isEmpty() )
     return "";
    if( mList.size() ==  1 )
      return mList.get(0).toString();
    return Util.joinValues(mList,ShellConstants.ARG_SEPARATOR );
    
  }
}
