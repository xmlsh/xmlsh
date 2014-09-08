/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
public class ShellAppender extends AbstractAppender  {

  private OutputStream os ;

  static PatternLayout mLayout = PatternLayout.createDefaultLayout();
  public ShellAppender(OutputStream os)
  {
    super("org.xmlsh.xmlshui", null, mLayout);
    this.os =os;
  }


  @Override
  public void append(LogEvent event)
  {
    try {
      os.write( event.getMessage().getFormattedMessage().getBytes());
    } catch (IOException e) {
      
    }
    
  }

}



/*
 * Copyright (C) 2008-2014   David A. Lee.
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