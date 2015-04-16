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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.xtypes.IXValueContainer;
import org.xmlsh.types.xtypes.IXValueList;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.types.xtypes.IXValueSequence;
import org.xmlsh.types.xtypes.XValueList;
import org.xmlsh.types.xtypes.XValueProperties;
import org.xmlsh.types.xtypes.XValueProperty;
import org.xmlsh.types.xtypes.XValueSequence;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Universal/Generic configuration object
 * 
 * Composed of XValueProperties in "sections"
 */

@JsonAutoDetect( fieldVisibility = Visibility.NONE , getterVisibility = Visibility.NONE, 
     setterVisibility = Visibility.NONE)
public class XConfiguration implements IXValueContainer, IXValueMap  {


    static Logger mLogger = LogManager.getLogger();

    private NameValueMap<XValueProperties> mSections;
    private String mDefaultSectionName;
    /* 
     * Default Configuration lookup strategy
     * 
     */
    public static class XConfigLookup extends XStringLookup {

        XConfiguration   mConfig;

        public XConfigLookup(XConfiguration config , XStringLookup parent ) {
            super(parent);
            mConfig = config;
        }

       
        /* (non-Javadoc)
         * @see org.xmlsh.core.XStringLookup#lookupXValue(java.lang.String)
         */
        @Override
        protected XValue lookupXValue(String name) {
            try {
                return mConfig.get(name);
            } catch (InvalidArgumentException e) {
                mLogger.catching(e);
            }
            return null ;
           
        }
    }


    public XConfiguration() {
        mSections = new NameValueMap<XValueProperties>();
    }

    public XConfiguration(NameValueMap<XValueProperties> sections) {
        mSections = sections;
    }
    public XConfiguration(NameValueMap<XValueProperties> sections,String defName ) {
        mSections = sections;
        mDefaultSectionName = defName ;
    }


    public XConfiguration withSection(String section, XValueProperties props) {
        addSection(section, props);
        return this;
    }

    public XValueProperties getSection(String section) {
        return mSections.get(section);
    }

    public XValueProperties getSection(String section, boolean bCreate) {

        XValueProperties props = mSections.get(section);
        if (props == null && bCreate)
            mSections.put(section, props = new XValueProperties());
        return props;
    }

    // Overrites section
    public void addSection(String section, XValueProperties properties) {
        mSections.put(section, properties);
    }

    // Merge properties on top of (replacing) all same-name values
    // return the merged properties
    public XValueProperties mergeSection(String section, XValueProperties properties) throws InvalidArgumentException {

        XValueProperties old = getSection(section);
        if (properties == null)
            return old;

        if (old != null)
            properties = old.merge(properties);

        mSections.put(section, properties);
        return properties;
    }

  //  @JsonInclude
 //   @JsonAnySetter
    public void setProperty(String section, XValueProperty prop) {
        getSection(section, true).put(prop);
    }

    public void setProperty(String section, String name, XValue value) {
        getSection(section, true).put(name, value);
    }

    public XValue getProperty(String section, String name, XValue defValue) {
         XValue value = getProperty( section , name );
         if( value == null )
             value = defValue ;
         return value ;
    }

    public XValue getProperty(String section, String name) {
        mLogger.entry( section , name );
        if( section == null )
            return null ;
        XValueProperties sect = getSection(section);
        XValue value = null ;
        if (sect != null)
            value = sect.getProperty(name);
        

         // default section 
        if( value == null ){
            XValueProperties s = getDefaultSection( );
            if( s != null )
                value = s.getProperty(name);
        }
        
        return mLogger.exit(value );
    
    }

    @Override
    public boolean isEmpty() {
        return mSections.isEmpty();
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @Override
    public boolean isList() {

        return true;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean isSequence() {
        return false;
    }

    @Override
    public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException {
        try (OutputStreamWriter ps = new OutputStreamWriter(out, opts.getInputTextEncoding())) {

            for (Entry<String, XValueProperties> section : mSections.entrySet()) {
                ps.write(Util.getNewlineString());
                ps.write("[" + section.getKey() + "]");
                ps.flush();
                XValueProperties prop = section.getValue();
                prop.serialize(out, opts);
            }

        }
    }

    @Override
    public XValue asXValue() throws InvalidArgumentException {
        return XValue.newXValue(TypeFamily.XTYPE, this);
    }

    @Override
    public IXValueContainer asXContainer() {

        return this;
    }

    @Override
    public IXValueMap asXMap() {
        return this;
    }

    @Override
    public IXValueList asXList() {
        return new XValueList(values());
    }

    @Override
    public IXValueSequence<? extends IXValueSequence<?>> asXSequence() {
        return new XValueSequence();
    }

    @Override
    public int size() {
        return mSections.size();
    }

    @Override
    public void removeAll() {
        mSections.clear();

    }

    @Override
    public Set<String> keySet() {
        return mSections.keySet();
    }

    @Override
    public Collection<XValue> values() {

        return Util.toList(iterator());
    }

    @Override
    public Iterator<XValue> iterator() {

        return new Iterator<XValue>() {

            Iterator<XValueProperties> iter = mSections.values().iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public XValue next() {
                try {
                    return iter.next().asXValue();
                } catch (InvalidArgumentException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();

            }
        };

    }

    @Override
    public XValue append(XValue item) {
        throw new UnsupportedOperationException();
    }
    
    
    // Get property in "section.value" format or just "value" to use default
    // Formats
    // property
    // section
    // dotted.section
    // section.property
    // dotted.section.property
    
   // @JsonInclude
  //  @JsonAnySetter
    @Override
    public XValue get(String name) throws InvalidArgumentException {
        assert( name != null);
        // Try a section first
        if( mSections.containsKey(name))
            return mSections.get(name).asXValue();
    
        // try a plain property
        if( name.indexOf(ShellConstants.kDOT_CHAR) < 0 )
            return getProperty(mDefaultSectionName, name);
         
        // right to left find kDOT_CHAR
        // TODO handle  [a.b] c.d=value   , [a.b.c] d=value
        StringPair pair = new StringPair( name , ShellConstants.kDOT_CHAR, false );
        
        return getProperty( pair.getLeft() , pair.getRight());
        
        
    }
    
   
    
    public XValue getProperty(String name) throws InvalidArgumentException {

        mLogger.entry(name);
        // 1) try a section first
        
        StringPair pair = new StringPair(name,ShellConstants.kDOT_CHAR);
        XValue value = null;
        
        
        // secion . name 
        if( pair.hasLeft()){
           value = getProperty( pair.getLeft() , pair.getRight() );
        }
        // default section 
        if( value == null ){
            XValueProperties section = getDefaultSection( );
            if( section != null )
                value = section.getProperty(name);
            
        }
        return mLogger.exit( value );
    }

    @Override
    public XValue put(String key, XValue value) {

        if (value.isInstanceOf(XValueProperties.class))
            addSection(key, value.asInstanceOf(XValueProperties.class));
        else
            throw new UnsupportedOperationException();
        return null;

    }

    @Override
    public boolean containsKey(String key) {
        // Simulate getting values ... could be key section.key including defaults
        
        if( mSections.containsKey(key) )
            return true ;
        XValueProperties defs = getDefaultSection();
        if( defs != null &&  defs.containsKey(key))
           return true ;
        
        // TODO handle  [a.b] c.d=value   , [a.b.c] d=value
        StringPair pair = new StringPair( key , ShellConstants.kDOT_CHAR, false );
        return pair.hasLeft() ? containsKey( pair.getLeft() , pair.getRight()) : false ;
        
        
    }
    
    // Not sure this should allow missing sections with default values
    // e.g
    // "notexist" "name"
    public boolean containsKey(String sectname, String key) {
        XValueProperties sect = getSection(sectname);
        
        if( sect != null && sect.containsKey(key) )
            return true ;
        
        XValueProperties defs = getDefaultSection();
        if( defs != null &&  defs.containsKey(key))
            return true ;
        return false ;
        
    }
    

    public void setDefaultSectionName(String defSection) {
        mDefaultSectionName = defSection ;

    }

    
    public String getDefaultSectionName( ) {
        return mDefaultSectionName;
    }
    
    public XValueProperties getDefaultSection() {
        if( mDefaultSectionName != null )
           return getSection( mDefaultSectionName );
        return null ;
    }

   // @JsonAnyGetter
    @JsonInclude
    @JsonValue
    public Map<String,XValueProperties> asMap() { 
       return mSections;
    }

	public XConfiguration replace(XStringLookup parent) {
		XConfiguration that = new XConfiguration();
		that.setDefaultSectionName(getDefaultSectionName());
		  XStringLookup lookup = getLookup(parent);
		  for( Entry<String, XValueProperties> sect : mSections.entrySet() ){
			  that.addSection( sect.getKey() ,  sect.getValue().replaceVariables(lookup));
		  }
		  return that ;
		  
	}

	public XConfigLookup getLookup(XStringLookup parent) {
		return new XConfiguration.XConfigLookup(this,parent);
	}


    
}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
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
 */
