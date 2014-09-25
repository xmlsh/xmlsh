package org.xmlsh.sh.module;

import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

/*
 * A "PName" is like a QName but not indirected through a URI
 * "name" ->  { null , name }
 * ":name" -> { "" , name }
 * "prefix:name" { "prefix" , "name"
 * 
 * Resolving a PName to a fully qualified name depends on context
 * 
 */
public class PName extends StringPair {
  public PName( String name ){
	  super( name , ':');
  }
  public PName( String prefix , String name  ){
	  super( prefix , name , ':');
  }
  public String getPrefix()
  {
	  return super.getLeft();
  }
  public String getName(){
	  return super.getRight();
  }
	public boolean hasPrefix(boolean nonblank ) {
		if( nonblank )
			return ! Util.isBlank(super.getLeft());
		else
			return super.getLeft() != null ;
	}
	
  
}
