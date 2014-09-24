/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util.text;

public abstract class ConfigScanner extends PropertiesScanner implements IConfigScanner
{

  private String sectionStart = "[";
  private String sectionEnd = "]" ;

  /* (non-Javadoc)
   * @see org.xmlsh.util.text.PropertiesScanner#onLine(java.lang.String)
   */
  @Override
  public final void onLine(String line)
  {
    if( line.startsWith(sectionStart) && line.endsWith(sectionEnd ) ) {
       onSectionStart(
         line.substring(
           sectionStart.length(), 
           line.length() - sectionEnd.length() ).trim() );
    } else
      super.onLine(line);
  }

  /**
   * @param sectionStart the sectionStart to set
   */
  public void setSectionStart(String sectionStart)
  {
    this.sectionStart = sectionStart;
  }

  /**
   * @param sectionEnd the sectionEnd to set
   */
  public void setSectionEnd(String sectionEnd)
  {
    this.sectionEnd = sectionEnd;
  }

  @Override
  public void onBlankLine()
  {
    
  }

  @Override
  public void onComment(String line)
  {
  }

  @Override
  public void onEndOfFile()
  {
    
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