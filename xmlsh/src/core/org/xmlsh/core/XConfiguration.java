/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.NameValue;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;

/*
 * Universal/Generic configuration object 
 * 
 * Composed of XValueProperties in "sections" 
 * 
 */
public class XConfiguration implements IXValueContainer<XConfiguration> , IXValueMap<XConfiguration>
{
  
  private NameValueMap<XValueProperties>  mSections;
  
  public XConfiguration() {
    mSections = new NameValueMap<XValueProperties>();
  }
  
  public XConfiguration(NameValueMap<XValueProperties> sections) {
    mSections = sections;
  }
  
  
  public XConfiguration withSection( String section ,  XValueProperties props ) {
    addSection( section , props ); 
    return this;
  }
  
  public XValueProperties getSection(String section) {
    return mSections.get(section);
  }
  public XValueProperties getSection(String section, boolean bCreate ) {
    
    XValueProperties props =  mSections.get(section);
    if( props == null && bCreate )
      mSections.put( section , props = new XValueProperties() );
    return props ;
  }
  
  
  // Overrites section
  public void addSection( String section , XValueProperties properties ) {
    mSections.put(section, properties);
  }
  
  
  // Merge properties on top of (replacing) all same-name values
  // return the merged properties
  public XValueProperties mergeSection( String section , XValueProperties properties) {
    
    XValueProperties old = getSection(section);
    if( properties == null )
      return old ;
    
    if( old != null ) 
      properties = old.merge( properties);
    
    mSections.put(section, properties);
    return properties ;
  }
  
  public void setProperty( String section, XValueProperty prop ) {
    getSection(section,true).put(prop);
  }

  public void setProperty( String section, String name , XValue value ) {
    getSection(section,true).put(name,value);
  }
  
  public XValue getProperty( String section , String name, XValue defValue  ) {
    XValueProperties sect = getSection(section);
    if( sect != null )
      return sect.get(name);
    else
    return defValue;
  }
  
  public XValue getProperty( String section , String name  ) {
    
    XValueProperties sect = getSection(section);
    if( sect != null )
      return sect.get(name);
    else
      return null;
  }

  @Override
  public boolean isEmpty()
  {
     return mSections.isEmpty();
  }

  @Override
  public boolean isMap()
  {
    return true ;
  }

  @Override
  public boolean isList()
  {

    return true ;
  }

  @Override
  public boolean isAtomic()
  {
    return false;
  }

  @Override
  public boolean isContainer()
  {
    return true ;
  }

  @Override
  public boolean isSequence()
  {
    return false;
  }

  @Override
  public void serialize(OutputStream out, SerializeOpts opts) throws IOException
  {
    try ( OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding() ) ){

    for( Entry<String, XValueProperties> section : mSections.entrySet() ) {
       ps.write("["+section.getKey() +"]");
       ps.write( Util.getNewlineString() );
       ps.flush();
       XValueProperties prop = section.getValue();
       prop.serialize( out , opts );
    }
      
    }
  }


  @Override
  public XValue asXValue()
  {
    return XValue.newXValue( TypeFamily.XTYPE , this );
  }

  @Override
  public IXValueContainer<? extends IXValueContainer<?>> asXContainer()
  {

    return this ;
  }

  @Override
  public IXValueMap<? extends IXValueMap<?>> asXMap()
  {
    return this ;
  }

  @Override
  public IXValueList<? extends IXValueList<?>> asXList()
  {
   return new XValueList(this.values());
  }

  @Override
  public IXValueSequence<? extends IXValueSequence<?>> asXSequence()
  {
    return null ;
  }

  @Override
  public int size()
  {
    return mSections.size();
  }

  @Override
  public void removeAll()
  {
    mSections.clear();
    
  }

  @Override
  public Set<String> keySet()
  {
    return mSections.keySet();
  }

  public Collection<XValue> values()
  {
    
   return  Util.toList( iterator() );
  }

  @Override
  public Iterator<XValue> iterator()
  {
    
      return new Iterator<XValue>() {

        Iterator<XValueProperties>  iter = mSections.values().iterator();

        @Override
        public boolean hasNext()
        {
          return iter.hasNext();
        }

        @Override
        public XValue next()
        {
          return iter.next().asXValue();
        }

        @Override
        public void remove()
        {
          throw new UnsupportedOperationException();
          
        }
      };
    
    }
  @Override
  public XValue append(XValue item)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public XValue get(String name)
  {
 
    XValueProperties sect = mSections.get(name);
    if( sect != null )
      return sect.asXValue();
    else
      return null ;
    
  }

  @Override
  public XValue put(String key, XValue value)
  {

    if( value.isInstanceOf( XValueProperties.class ) )
      addSection( key , value.asInstanceOf(XValueProperties.class ) );
    else
      throw new UnsupportedOperationException();
    return null;

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