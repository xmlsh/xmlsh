/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.sh.grammar.Token;

public class SourceLocation {
	private		String	mSource;
	private		int		mStartLine;
	private		int		mStartColumn;
	private		int		mEndLine;
	private		int		mEndColumn;
	public SourceLocation(String source, int startline, int startColumn, int endLine, int endColumn) {
		mSource = source;
		mStartLine = startline;
		mStartColumn = startColumn;
		mEndLine = endLine;
		mEndColumn = endColumn;
	}
	
	public SourceLocation( String source , Token token )
	{
		mSource = source ;
		if( token != null ){
			mStartLine = token.beginLine;
			mEndLine = token.endLine;
			mStartColumn = token.beginColumn;
			mEndColumn = token.endColumn;
		}
	}
	
	
	/**
	 * @return the source
	 */
	public String getSource() {
		return mSource;
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
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if( mSource != null )
			sb.append( mSource );
		else
			sb.append("stdin");
		
		//sb.append("] ");
		// sb.append("Start (line,col): ");
		sb.append(" line: ");
		sb.append( String.valueOf(mStartLine) );
		/*
		sb.append( "," );
		sb.append( String.valueOf(mStartColumn));
		sb.append("  End:");
		sb.append( String.valueOf(mEndLine) );
		sb.append( "," );
		sb.append( String.valueOf(mEndColumn));
		*/
		sb.append("]");

		return sb.toString();
	}
	
}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
