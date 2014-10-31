/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.xmlsh.types.TypeFamily;


/*
 * 
 * Generic Properties 
 * A set of Name/Value pairs to any object 
 * 
 * 
 */
public class XValueProperties extends XValueMap  {
	

  public XValueProperties( Map<?,?> map  ) throws InvalidArgumentException {
    for(Map.Entry<?, ?> e : map.entrySet() ) {
      super.put( e.getKey().toString() , XValue.newInstance(e.getValue()));
    }
	}
	
	

  public XValueProperties() {
    super();
	}

  public XValueProperties(XValueProperty prop) {
  
	  super();
	  add(prop);
	  
  
   }



	
	
	public XValueProperties(List<XValueProperty> list) {
 
		for( XValueProperty p : list )
			add(p);
			
		
	}



public static XValueProperties fromMap( Map<?,?> map ) throws InvalidArgumentException {
    return new XValueProperties( map  );
  };
  public static XValueProperties fromJavaProperties( Properties props ) throws InvalidArgumentException {
    return new XValueProperties( props  );
  };
  
  public static XValueProperties fromProperty( XValueProperty prop ){
	    return new XValueProperties( prop  );
  }
  public static XValueProperties fromPropertyList( List<XValueProperty> list ){
	  return new XValueProperties(list);
  }
  
  @Override
  public
  XValue asXValue() throws InvalidArgumentException {
    return XValue.newXValue( TypeFamily.XTYPE , this );
  }



  /*
   * Expand properties to set of nested properties based on a delimiter
   */
  public XValueProperties expandTree( String delim ) throws InvalidArgumentException {
    
    
      
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
    private XValueProperties nestedProperties(  Set<String> keySet , String parent , String delim ) throws InvalidArgumentException
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
  
  /*
   * Merge these properties with that, recursively
   * return a merged set where any property in that overwrites the property in this
   *  
   */
  public XValueProperties merge( XValueProperties that) throws InvalidArgumentException 
  {
    if( isEmpty() )
      return that ;
    
    // Copy this to merged 
    XValueProperties merged = new XValueProperties(this);
    
    Set<java.util.Map.Entry<String, XValue>> entries =  that.entrySet();
    for( java.util.Map.Entry<String, XValue> e : entries ) {
      XValue thatx = e.getValue();
      // Nothing in this - put it in 
      String key = e.getKey();
      if( ! merged.containsKey( key) )
        merged.put( e );
      else {
         
        XValue thisx = get( key );
        assert(thisx !=null );
        
        // Both are nested properties, merge 
        if( thisx.isInstanceOf( XValueProperties.class ) &&
            thatx.isInstanceOf( XValueProperties.class )   ) {
          
             XValueProperties thatp = thatx.asInstanceOf( XValueProperties.class );
             XValueProperties thisp = thisx.asInstanceOf( XValueProperties.class );
             merged.put( key , thisp.merge(  thatp ));
        } else
          merged.put( key , thatx );
      }
    }
    return merged ;
  }



	public void replaceVariables(final StrLookup<String> lookup) {
		
		final XValueProperties props  = this ;
		StrLookup<String> look  = new StrLookup<String>( ) {

			@Override
			public String lookup(String key) {
				String value = lookup.lookup(key);
				if( value == null ){
					XValue xv = props.get(key);
					if( xv != null && xv.isAtomic())
						value = xv.toString();
			
				}
				return value ;
			}
		};
		
		StrSubstitutor subst = new StrSubstitutor( look );
		subst.setEnableSubstitutionInVariables(true);
		
		for( java.util.Map.Entry<String, XValue> e : entrySet() ){
			XValue v = e.getValue();
			if( v.isAtomic() ){
				StringBuilder sb = new StringBuilder( v.toString() );
				if( subst.replaceIn(sb) )
					e.setValue( XValue.newXValue(sb.toString()));
			}
			
		}
		
	}


	public static  XValueProperties fromXValue(XValue xv) throws InvalidArgumentException
	{
		
		return fromXValues( Collections.singletonList(xv));
	}

	public static  XValueProperties fromXValues(List<XValue> args)
			throws InvalidArgumentException {
		XValueProperties props = null;
		for (XValue xarg : args) {
			for (XValue arg : xarg) {
				if (props == null) {
					if (arg.isInstanceOf(XValueProperties.class))
						props = arg.asInstanceOf(XValueProperties.class);
					else if (arg.isInstanceOf(Map.class))
						props = fromMap(arg
								.asInstanceOf(Map.class));
					else if( arg.isInstanceOf(XValueProperty.class))
						props = new XValueProperties(arg.asInstanceOf(XValueProperty.class));
					else if( arg.isInstanceOf( IXValueMap.class )) {
						props = new XValueProperties( (Map<?, ?>) arg.asInstanceOf( IXValueMap.class ));
						
					}
	
				} else {
					if (arg.isInstanceOf(XValueProperties.class))
						props = props.merge(arg
								.asInstanceOf(XValueProperties.class));
					else if (arg.isInstanceOf(Map.class))
						props = props.merge(fromMap(arg
								.asInstanceOf(Map.class)));
					else if( arg.isInstanceOf(XValueProperty.class))
						props.add( arg.asInstanceOf(XValueProperty.class));
					else if( arg.isInstanceOf( IXValueMap.class )) {
						props = props.merge( fromMap((Map<?, ?>) arg.asInstanceOf( IXValueMap.class )));
					}
					else {
						
					}
				
	
				}
			}
		}
		return props;
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