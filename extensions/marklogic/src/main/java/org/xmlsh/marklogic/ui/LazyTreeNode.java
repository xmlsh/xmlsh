/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class LazyTreeNode extends DefaultMutableTreeNode {

	private boolean loaded;
	private String url;
	private String displayName;

	private int start = -1;
	private int end = -1;
	
	


	public LazyTreeNode(String url) {
		super(null  , false);

		this.url = url ;
		this.displayName = getDisplay(url);
		this.start = -1 ;
		this.end   = -1;

		
	}

	public LazyTreeNode(String url, String displayName ) {
		super(null  , false);

		this.url = url ;
		this.displayName = displayName ;
		start=end = -1 ;

		
	}
	

	public LazyTreeNode(String url, String displayName , boolean allowsChildren ) {

		this( url , displayName , allowsChildren , -1 , -1 );
		
	}

	

	LazyTreeNode()
	{
		this( "" , "" , true , -1 , -1 );
	}

	
	public LazyTreeNode(String url, String displayName, boolean allowsChildren, int start , int end ) {
		super(null  , allowsChildren);

		this.url = url ;
		this.displayName = displayName ;
		this.start = start ;
		this.end = end ;

	}
	

	protected boolean isLoaded() {
		return loaded;
	}

	protected void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	@Override
	public boolean isLeaf() {
		return !getAllowsChildren();
	}
	
	public String toString() { 
		
		return  getDisplayName() ;
		
	}
	private static String getDisplay(String s) {
		if( Util.isBlank(s ) )
			return "<Loading ...>";
		return s ;
	}
	
	boolean isDirectory(){
		return url != null && url.endsWith("/");
	}


	/**
	 * @return the url
	 */
	protected String getUrl() {
		return url;
	}


	/**
	 * @return the displayName
	 */
	protected String getDisplayName() {
		if( start == end  )
		 return displayName;
		else
			return displayName + "  [" + start + "..."+end+ "]";
		
	}

	public void setLength(int start, int end) {

		this.start = start ;
		this.end = end ;

	}

	/**
	 * @return the start
	 */
	protected int getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	protected int getEnd() {
		return end;
	}


	

}

/*
 * Copyright (C) 2008-2014 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */