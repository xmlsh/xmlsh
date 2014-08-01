/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.InputDecorator;

public class JSONPInputDecorator extends InputDecorator
{


	public JSONPInputDecorator()
	{
	}

	@Override
	public InputStream decorate(IOContext ctxt, InputStream in) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream decorate(IOContext ctxt, byte[] src, int offset, int length) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader decorate(IOContext ctxt, Reader r) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
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