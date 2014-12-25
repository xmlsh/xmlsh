/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util.text;

import java.io.IOException;
import java.io.Reader;

import org.xmlsh.core.InvalidArgumentException;


/*
 * A TextLine parser based on a Scanner object thats preconfigured
 * Trims trailing whitespace and reports comments and blank lines
 * 
 */
public abstract class TextLineScanner implements ITextLineParser
{
  private String[] beginOfLineComments = { "#"  ,  ";" } ; // Must start at SOL
  private String[] lineComments = { "#"  ,  ";" } ;         // Comments rest of line
  private String currentLine  = null ;
  private boolean escapeEOL = true ;
  private boolean escapeLiteral = true ;
  private boolean ignoreBlankLines = true ;
  private boolean trimEnd = true ;
  private boolean trimStart = false ;
  

  public TextLineScanner()
  {
  }
  

  void run(Reader r) throws InvalidArgumentException, IOException {
	  String line ;
      while((line = readLine( r )) != null ) {


    	  // Comments are before trimming 
          if( lineComments != null && line.length() > 0 ){
        	  int pos = findFirst( line , lineComments );
        	  if( pos >= 0 ){
        	    String comment = line.substring(pos+1);
        	    line = line.substring(0,pos);
        	    onComment(comment);
        	  }
          }
          if( beginOfLineComments != null && line.length() > 0  ){
        	  int pos  = startsWith( line , beginOfLineComments ); 
        	  if( pos > 0 ){
        	    String comment = line.substring(pos+1);
        	    line = "" ;
        	    onComment(comment);
        	    continue ;
        	  }
          }       
          if( trimEnd )
        	  line = trimEnd(line);
          if( trimStart )
        	  line = trimStart(line);
          
          if( ignoreBlankLines &&  line.length() == 0 )
        	  continue;
          
          onLine( currentLine = line );
          
          
      }
      onEndOfFile();
  }
  private String trimStart(String line) {
	int pos = 0;
	while( pos < line.length() && Character.isWhitespace(line.charAt(pos)))
		pos++;
	if( pos > 0 )
		return line.substring(pos);
	return line ;
}


  
// Returns the position of a line *after* the longest substring initial match
private int startsWith(String line, String[] substrings) {
	int len = -1;
	for(String s : substrings ){
		if( line.startsWith(s))
			len = Math.max( len ,  s.length() );
	}
	return len ;
}


// Finds the first position of any substring 
private int findFirst(String line, String[] substrings) {
	int pos = -1;
	for( String s : substrings ){
		int spos = line.indexOf( s );
		if( spos >= 0 ){
			if( pos < 0 )
				pos = spos;
			else
				pos = Math.min( spos , pos );
		}
	}
	return pos ;
}


private String trimEnd(String line) 
{
	int pos = line.length() - 1;
	while( pos >= 0 && Character.isWhitespace(line.charAt(pos) ))
			pos--;
	if( pos < line.length() - 1 )
		return line.substring( 0 , pos + 1 );
	return line ;
	
	
}


private String readLine(Reader r) throws IOException {
	  StringBuilder sb = null ;
	  int c ;
	  while(( c = r.read()) >= 0 ){
		  if( sb == null )
			  sb = new StringBuilder();
		  if( c == '\r')
			  continue ;
		  if( c == '\n')
			  break ;
		  if( c == '\\'){
			  c = r.read();
			  if( c == '\r' )
				  c = r.read();
			  
			  if( c < 0 ){
				  sb.append( '\\');
				  break ;
			  }
			  if( escapeLiteral ){
				  switch( c ){
				  case 'b' : sb.append( '\b') ; continue ;
				  case 'n' : sb.append( '\n') ; continue ;
				  case 't' : sb.append( '\t') ; continue ;
				  case 'f' : sb.append( '\f') ; continue ;
				  default : break ;
				  }
		     }
			 if( escapeEOL && c == '\n' )
				 continue ;
		  } 
			 sb.append( ( char) c );
	  }
	return sb == null ? null : sb.toString();
}


protected String getCurrentLine() { 
    return currentLine;
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