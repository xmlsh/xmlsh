/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.types.xtypes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

/*
 * A list of objects indexable by string or index (1 based for strings)
 */
public class XValueList  extends AbstractList<XValue> implements IXValueContainer<XValueList>, IXValueList<XValueList> 
{
	private  	List<XValue>   mList;

	public XValueList() {
		mList = new LinkedList<>();
	}
  public XValueList(Collection<XValue> collection ) {
    mList = new LinkedList<>(collection );
  }
  
	
	public XValueList(XValueList that) {
	    mList = new LinkedList<>( that.mList );
    }

    @Override
	public boolean add( XValue value ) {
		return mList.add(value);
	}

	@Override
	public XValue getAt( int pos) {
		if( pos < 0 || pos >= mList.size() )
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
	public void removeAll() {
		mList.clear();

	}
	
  /* (non-Javadoc)
   * @see org.xmlsh.core.IXValueList#setAt(int, org.xmlsh.core.XValue)
   */
  @Override
  public XValue setAt(int index, XValue value)
  {
    return mList.set(index, value);
  }

  

	@Override
	public Collection<XValue> values()
	{
		return Collections.unmodifiableCollection(mList);
	}

	public void addAll(List<XValue> args)
	{
		mList.addAll(args);
	}

	@Override
	public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException
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
		}

	}

	/* (non-Javadoc)
   * @see org.xmlsh.core.IXValueList#get(int)
   */
  @Override
	public XValue get(int index)
	{
		return getAt(index);
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

    @Override
    public XValue append(XValue item) throws InvalidArgumentException {
        XValueList newList = new XValueList(this);
        newList.add(item);
        return XValue.newXValue( TypeFamily.XTYPE , newList ); 
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
      return false;
    }
    @Override
    public IXValueContainer<? extends IXValueContainer<?>> asXContainer()
    {
      return this;
    }
    @Override
    public IXValueMap<? extends IXValueMap<?>> asXMap()
    {
      return  null;
    }
    @Override
    public IXValueList<? extends IXValueList<?>> asXList()
    {
      return this;
    }
    @Override
    public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
    {
      return new XValueSequence(mList);
    }
    @Override
    public List<XValue> asList()
    {
      return this ;
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