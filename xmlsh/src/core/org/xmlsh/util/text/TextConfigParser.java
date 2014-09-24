/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util.text;

import java.util.Scanner;

import org.xmlsh.core.XConfiguration;
import org.xmlsh.core.XValue;

public abstract class TextConfigParser
{
  private String currentSection ;
  private XConfiguration config  = new XConfiguration();

  protected abstract XValue parseValue(String currentSection, String name, String value);
  
  private final class ConfigHelper extends ConfigScanner
  {
    @Override
    public void onSectionStart(String section)
    {
      currentSection = section ;
    }

    @Override
    public void onProperty(String name, String value)
    {
      if( currentSection == null )
        throw new IllegalArgumentException(
          "Properties are not allowed outside a section: Current Line: " + getCurrentLine() );
     config.setProperty(currentSection ,  name, parseValue( currentSection , name , value  ) ); 
      
    }

   
  }

  
  public XConfiguration loadConfig( Readable r ) {
    ConfigScanner  cs = new ConfigHelper();
    try( Scanner s =  new Scanner(r) )
    {
       cs.run( s );
    }
    
    return config ;
    
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