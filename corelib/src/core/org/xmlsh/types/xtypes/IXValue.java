/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.types.xtypes;

import java.io.IOException;
import java.io.OutputStream;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;


/*
 * All FamilyType.XTYPE Objects derive implement this interface
 * 
 */
public interface IXValue
{

  public boolean isEmpty();
  public boolean isMap();
  public boolean isList();  
  public boolean isAtomic();
  public boolean isContainer();
  public boolean isSequence();
  public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException;
  public XValue append(XValue item) throws InvalidArgumentException;
  public XValue asXValue() throws InvalidArgumentException;
  public IXValueContainer asXContainer();
  public IXValueMap asXMap();
  public IXValueList asXList();
  public IXValueSequence<? extends IXValueSequence<?>> asXSequence();

  
  
  
  

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