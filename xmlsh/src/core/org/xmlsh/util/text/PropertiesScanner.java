/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util.text;

import java.util.Scanner;

public abstract class PropertiesScanner extends TextLineScanner implements IPropertiesScanner
{
  
  String propertyDelimiter = "[=:]";
  
  void run( Scanner scanner ) {
    super.run(scanner);
    
  }
  void setDelimiter( String delim ) {
    propertyDelimiter = delim ;
  }
  

  /* (non-Javadoc)
   * @see org.xmlsh.util.text.ITextLineParser#onLine(java.lang.String)
   */
  @Override
  public void  onLine(String line)
  {
    String[] pair = line.split( propertyDelimiter , 2 );
    if( pair.length != 2 )
      throw new IllegalArgumentException(
        "Invalid property format: no " + propertyDelimiter + " pattern is found in the line ["
                + line + "].");
      
    String key = pair[0].trim();
    String value = pair[1].trim();
    onProperty( key , value );
    
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