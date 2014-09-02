/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/*
 * 
 * Generic Properties 
 * A set of Name/Value pairs to any object 
 * Supports "parent" properties aka Java Properties to provide default values.
 * Supports property "trees" by allowing any value to be any XValue including a List or Propert
 * 
 * 
 * 
 */
public class XValueProperties extends XValueMap {

  private XValueProperties mDefaults = null;

  /*
   */
  
	public XValueProperties(XValueProperties defaults) {
	  mDefaults = defaults;
	}
	

  protected XValueProperties( Map<?,?> map , XValueProperties defaults ) {
    this(defaults);
	  for( java.util.Map.Entry<?, ?> entries : map.entrySet() ) 
	    put( XValue.newXValue(entries.getKey()).toString() ,  XValue.newXValue(entries.getValue())  );
	}
	
	

  public XValueProperties() {
    super();
	}

  public static XValueProperties fromMap( Map<?,?> map ) {
    return new XValueProperties( map , null );
  };
  public static XValueProperties fromJavaProperties( Properties props ) {
    return new XValueProperties( (Map<?,?>) props , null   );
  };
  
  @Override
  public
  XValue asXValue() {
    return XValue.newXValue( TypeFamily.XTYPE , this );
  }


  /* (non-Javadoc)
   * @see org.xmlsh.core.XValueMap#removeAll()
   */
  @Override
  public void removeAll()
  {
    super.removeAll();
    mDefaults = null;
  }


  /* (non-Javadoc)
   * @see org.xmlsh.core.XValueMap#keySet()
   */
  @Override
  public Set<String> keySet()
  {
    if( mDefaults == null )
      return super.keySet();
    
    HashSet<String> keys = new HashSet<>(super.keySet());
    keys.addAll( mDefaults.keySet() );
    return keys;
  }


  /* (non-Javadoc)
   * @see org.xmlsh.core.XValueMap#get(java.lang.String)
   */
  @Override
  public XValue get(String name)
  {
    XValue v = super.get(name);
    if( v == null && mDefaults != null )
      v = mDefaults.get(name);
    return v;
  }
  
  
  /*
   * Expand properties to set of nested properties based on a delimiter
   */
  public XValueProperties expandTree( String delim ) {
    
    
      
    XValueProperties tree = new XValueProperties();
    Set<String> keySet =  keySet();
    Set<String> nested = nestedKeys(keySet , delim );
    if( nested.isEmpty() ) {
      tree.addAll(this);
      return tree;
    }
    
    List<String> rootKeys = topLevelKeys( keySet , delim );
    for( String key : rootKeys ) 
      tree.put( key , get(key) );
    
    for( String key : nested ){
      XValueProperties nestedProps = nestedProperties(  keySet , key , delim  );
      if( nestedProps != null )
        tree.put(key, nestedProps);
    }  
    
    return tree ;
  }
    
    
    
 // Create a new property with only keys nested under the parent
    private XValueProperties nestedProperties(  Set<String> keySet , String parent , String delim )
    {
      System.out.println("nestedProperties: parent: " + parent );
      // Create a new Properties object by filtering out only keys starting with parent.
      XValueProperties nested = new XValueProperties();
      String parentKey = parent + delim ; 
      int parentLen = parentKey.length();
      for( String key : keySet  ){
        if( key.startsWith(parentKey)){
          String childKey = key.substring(parentLen);
          
          XValue value = get(key);
          nested.put( childKey , value );
          if( childKey.contains(delim) ) {
            XValueProperties childProps = nestedProperties( keySet , childKey , delim );
            if( childProps  != null ) 
              nested.put( childKey , childProps.asXValue());
            
          } else
            nested.put( childKey , value );
        }
      }
      if( nested.isEmpty() )
        return null ;
      return nested.expandTree(delim);
    }
        
//Return all unique keys that start with a common prefix , stripping off any child suffexes
 private static Set<String> nestedKeys(Set<String> keys ,  String delim )
 {
   Set<String>  nestedKeys = new HashSet<String>();
   for( String key :keys  ){
     // many identical prefixes allowed one key per name so dont need to find dups
     int dpos = key.indexOf(delim);
     if( dpos > 0 )
       nestedKeys.add(key.substring(0,dpos));
   }
   return nestedKeys;
 }
 

  private static List<String> topLevelKeys( Set<String> keys ,  String delim )
  {
    List<String>  topKeys = new ArrayList<String>();
    for( String key : keys ){
      // only alllowed one key per name so dont need to find dups
      if( !key.contains(delim))
        topKeys.add(key);
    }
    return topKeys;
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