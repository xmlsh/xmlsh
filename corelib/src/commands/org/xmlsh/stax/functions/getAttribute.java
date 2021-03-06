/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.stax.functions;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.trans.XPathException;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StAXUtils;

public class getAttribute extends AbstractBuiltinFunction {



	public getAttribute()
	{
		super("getAttribute");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws CoreException, XPathException, XMLStreamException {
		if( args.size()  < 2  )
			return null;
		Object arg = args.get(0).asObject();
		QName attrName = args.get(1).asQName(shell);

		if( arg instanceof XMLEvent )
		{
			XMLEvent event = (XMLEvent) arg;
			if( event.isStartElement()) {
				Attribute attr = event.asStartElement().getAttributeByName(StAXUtils.getQName(attrName));
				return XValue.newXValue(attr == null ? null : attr.getValue());
			} else
				return null ;
		}
		else
			return null ;

	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
