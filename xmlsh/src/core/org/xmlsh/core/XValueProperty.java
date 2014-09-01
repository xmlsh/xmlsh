/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;
import org.xmlsh.util.XNamedValue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/*
 * A single "Property" - substitutable for a Map 
 *  
 */
public class XValueProperty  extends XNamedValue  implements IXValueMap<XValueProperty>
{
	
	public XValueProperty(String name, XValue value)
    {
	    super(name, value);
    }
  public XValueProperty( XValue value)
  {
    super(null,value);
  }

	@Override
    public int size()
    {
	   return 1;
    }

	@Override
    public boolean isEmpty()
    {
		return getValue() == null  ;
    }

	  @Override
    public XValue get(String name)
    {
	   return  Util.isEqual(name, getName() ) ? getValue() : null ;
    }

	@Override
    public void removeAll() throws UnsupportedOperationException
    {
		throw new UnsupportedOperationException("removeAll is not implemented for XValueProperty");
	    
    }

	@Override
    public Set<String> keySet()
    {
	   return Collections.singleton(getName());
    }

	@Override
    public Collection<XValue> values()
    {
	    return Collections.singletonList(getValue());
    }

	@Override
    public void serialize(OutputStream out, SerializeOpts opts) throws IOException
    {

	  
       XValue value = getValue();
       if( value == null )
         return ;
       value.serialize(out, opts);

	  
	  
    }

	@Override
    public boolean isMap()
    {
	    return true;
    }

	@Override
    public boolean isList()
    {
	    return true;
    }

	@Override
    public boolean isAtomic()
    {
	    return true;
    }
	
	
	/*
	 * operator += 
	 * @see org.xmlsh.core.IXValueContainer#append(org.xmlsh.core.XValue)
	 */

    @Override
    public XValue append(XValue item) {
        
        if( item.isEmpty() )
            return XValue.asXValue(this) ;
        
        XValuePropertyList newMap = new XValuePropertyList( this  );
        newMap.add( XValueProperty.instanceOf(item) );
        return XValue.asXValue( newMap  );
        
        
        
        
    }

    @Override
    public Iterator<XValue> iterator() {
       return Collections.singletonList( getValue()).iterator();
    }

    // Allows null or "" as equivilent names
    public boolean nameEquals(String name)
    {
       return Util.isEqual( getName() , name  );
      
    }
    public static XValueProperty instanceOf(XValue item)
    {
      if( item.isInstanceOf( XValueProperty.class ) )
       return (XValueProperty) item.asObject();
      else 
        return new XValueProperty(item) ;
    }
    
    @Override
    public XValue asXValue()
    {
      return XValue.asXValue( TypeFamily.XTYPE , this );
    }
    @Override
    public boolean isContainer()
    {
      return true;
    }
    @Override
    public boolean isSequence()
    {
      return false ;
    }
    @Override
    public IXValueContainer<? extends IXValueContainer<?>> asXContainer()
    {
      return this;
    }
    @Override
    public IXValueMap<? extends IXValueMap<?>> asXMap()
    {
      return this;
    }
    @Override
    public IXValueList<? extends IXValueList<?>> asXList()
    {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
    {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public XValue put(String key, XValue value)
    {
      throw new UnsupportedOperationException("put not supported for XValueProperty");
        
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