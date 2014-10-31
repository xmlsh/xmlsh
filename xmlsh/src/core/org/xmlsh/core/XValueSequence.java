package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;


/*
 * A "Sequence" of XValues which is analogous to an XdmValue which is 0 or more XdmItems
 * Sequences are "flat" that is if other sequences are added to them as children the the children are added instead
 * 
 * Since an XValue is a sequence itself then 
 * only lists of > 1 XValues need be implemented
 */
public  class XValueSequence implements Iterable<XValue>  ,    IXValueSequence<XValueSequence>
{
  private static IXValueSequence<XValueSequence> _emptySequence = new XValueSequence();

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
  
  
  public static IXValueSequence<XValueSequence> emptySequence() {
    return _emptySequence;
  }
  

  /* (non-Javadoc)
   * @see org.xmlsh.core.IXValueSequence#addValue(org.xmlsh.core.XValue)
   */
  @Override
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


  @Override
public int size()
  {
    return mList.size();
  }
  

  @Override
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




  /* (non-Javadoc)
   * @see org.xmlsh.core.IXValueSequence#getAt(int)
   */
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
  public Collection<XValue> values()
  {
    return Collections.unmodifiableCollection(mList);
  }


  @Override
  public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException
  {
    ( new XValueList( mList )).serialize(out, opts);
  }


  @Override
  public XValue append(XValue item) throws InvalidArgumentException
  {
    IXValueSequence<?> s = new XValueSequence(this);
    s.addValue(item);

    return XValue.newXValue( TypeFamily.XTYPE , s );
    
  }

  /* (non-Javadoc)
   * @see org.xmlsh.core.IXValueSequence#setAt(int, org.xmlsh.core.XValue)
   */
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



  @Override
  public XValue asXValue() throws InvalidArgumentException
  {
    return XValue.newXValue( TypeFamily.XTYPE , this );
  }



  @Override
  public boolean isContainer()
  {
    return true;
  }



  @Override
  public boolean isSequence()
  {
    return true;
  }




  @Override
  public IXValueContainer<? extends IXValueContainer<?>> asXContainer()
  {
    return this;
  }



  @Override
  public IXValueMap<? extends IXValueMap<?>> asXMap()
  {
    return null;
  }



  @Override
  public IXValueList<? extends IXValueList<?>> asXList()
  {
    return new XValueList(mList);
  }



  @Override
  public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
  {
    return this;
  }



  @Override
  public IXValueSequence subSequence(int begin)
  {
    return new XValueSequence( mList.subList(begin, mList.size() ));
  }
}
