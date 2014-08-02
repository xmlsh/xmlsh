/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;
import org.xmlsh.util.XNamedValue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/*
 * A single "Property" - substitutable for a Map 
 *  
 */
public class XValueProperty  extends XNamedValue  implements IXValueContainer<XValueProperty> 
{
	
	public XValueProperty(String name, XValue value)
    {
	    super(name, value);
    }

	@Override
    public int size()
    {
	   return 1;
    }

	@Override
    public boolean isEmpty()
    {
		return false ;
    }

	@Override
    public XValue put(String key, XValue value) throws UnsupportedOperationException
    {
		throw new UnsupportedOperationException("put is not implemented for XValueProperty");
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

		try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){
				ps.write( getName());
				ps.write(":=");
				ps.flush();
				getValue().serialize(out, opts);
		} catch (InvalidArgumentException e) {
			Util.wrapIOException(e);
		}		
    }

	@Override
    public boolean add(XValue arg)
    {
		throw new UnsupportedOperationException("removeAll is not implemented for XValueProperty");
    }

	@Override
    public boolean isMap()
    {
	    // TODO Auto-generated method stub
	    return true;
    }

	@Override
    public boolean isList()
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean isAtomic()
    {
	    // TODO Auto-generated method stub
	    return false;
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