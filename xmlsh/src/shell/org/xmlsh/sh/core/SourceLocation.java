/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.File;
import java.nio.file.Paths;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

public class SourceLocation implements Cloneable {
	private static final String kLOCATION_FORMAT = "[%s%s line: %d]";
	private    String  mName;  // "" , script , source , function
	private		String	mSource;
	private		int		mStartLine;
	private		int		mStartColumn;
	private		int		mEndLine;
	private		int		mEndColumn;
	private boolean kDEFAULT_LOCATION_FORMAT = false ; // TODO Enum
	
	public SourceLocation(String source, String name ,  int startline, int startColumn, int endLine, int endColumn) {
		mName = name ;
		mSource = source;
		mStartLine = startline;
		mStartColumn = startColumn;
		mEndLine = endLine;
		mEndColumn = endColumn;
	}
	public SourceLocation( String source , Token token )
	{

		mSource = source ;
		mName = null ;
		if( token != null ){
			mStartLine = token.beginLine;
			mEndLine = token.endLine;
			mStartColumn = token.beginColumn;
			mEndColumn = token.endColumn;
		}
	}
	
	
	public SourceLocation() {
		mSource = null ; 
		mName = null ;
	}
	
	public SourceLocation(SourceLocation that ) {
		this( that.mSource , that.mName , that.mStartLine, that.mStartColumn , that.mEndLine , that.mEndColumn );
	}

	public SourceLocation(String name, SourceLocation that) {
		this(that);
		setName(name);

	}
	/**
	 * @return the source
	 */
	
	public String getName() {
		return Util.notNull(mName);
	}
	public String getSource() {
		return getSource(false);
	}

	public String getSource(boolean relpath) {
		String src =  Util.notNull(mSource);
		return relpath ? FileUtils.getPathLikeName( src ) : src ;
	}

	/**
	 * @return the startline
	 */
	public int getStartline() {
		return mStartLine;
	}
	/**
	 * @return the startColumn
	 */
	public int getStartColumn() {
		return mStartColumn;
	}
	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return mEndLine;
	}
	/**
	 * @return the endColumn
	 */
	public int getEndColumn() {
		return mEndColumn;
	}
	public String format(boolean relpath ) {
		return String.format(kLOCATION_FORMAT ,
		hasSource() ? getSource(relpath) : "stdin" ,
        hasName() ? String.format("function %s() ",mName) : "" ,
        getStartLine() );
	}
	

	public boolean hasSource() {
	  return ! Util.isBlank(mSource);
	}
	public String toString()
	{
		return format(kDEFAULT_LOCATION_FORMAT);
	}

	/**
	 * @return the startLine
	 */
	public int getStartLine() {
		return mStartLine;
	}

	/**
	 * @param startLine the startLine to set
	 */
	public void setStartLine(int startLine) {
		mStartLine = startLine;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		mSource = source;
	}

	/**
	 * @param startColumn the startColumn to set
	 */
	public void setStartColumn(int startColumn) {
		mStartColumn = startColumn;
	}

	/**
	 * @param endLine the endLine to set
	 */
	public void setEndLine(int endLine) {
		mEndLine = endLine;
	}

	/**
	 * @param endColumn the endColumn to set
	 */
	public void setEndColumn(int endColumn) {
		mEndColumn = endColumn;
	}


	public void refineLocation(SourceLocation loc) {
		if( mName == null && loc.mName != null )
			mName = loc.mName;
		if( mSource == null && loc.mSource != null )
			mSource = loc.mSource;
		
	}
	public boolean isEmpty() {
		return mStartLine <= 0 ;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException  {
			return super.clone();
	}
	public boolean hasName() {
		return ! Util.isBlank(mName);
	}
	
	// fake for now just parse as boolean
	public static boolean parseFormat(XValue value) throws InvalidArgumentException
    {
		try {
	        return value.toBoolean();
        } catch (UnexpectedException e) {
	       Util.wrapException(e, InvalidArgumentException.class);
	       // SNH
        }
		return false ;
    }
	
	
	
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
