/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.types;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

import java.io.IOException;
import java.io.OutputStream;

/*
 * Generic methods available on typed objects
 */
public interface IMethods
{
	public XValue append(Object value, XValue v);
	public String asString( Object obj );
	public int    getSize( Object obj );
	public IType  getType( Object obj );
	public XValue getXValue( Object obj );
	public XValue getXValue(Object obj, String ind) throws CoreException;
	public void   serialize( Object obj , OutputStream os , SerializeOpts opts) throws IOException ;
	public String  simpleTypeName(Object obj);
	public String  typeName(Object obj);     // specific type name 

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