/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util.text;

import java.util.Scanner;

import org.xmlsh.util.Util;


/*
 * A TextLine parser based on a Scanner object thats preconfigured
 * Trims trailing whitespace and reports comments and blank lines
 * 
 */
public abstract class TextLineScanner implements ITextLineParser
{
  private String beginOfLineComment = "#" ;
  private String currentLine  = null ;

  public TextLineScanner()
  {
  }
  
  public void setBeginLineComment( String s ) {
    beginOfLineComment = s ;
  }

  void run(Scanner scanner) {
      while(scanner.hasNextLine()) {
          String line = currentLine = scanner.nextLine().trim();
          if( line.isEmpty() )
            onBlankLine();
          else 
          if( ! Util.isEmpty(beginOfLineComment ) && line.startsWith( beginOfLineComment ) )
            onComment( line );
          else
            onLine( line );
      }
      onEndOfFile();
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