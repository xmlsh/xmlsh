/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public abstract class AWSSDBCommand extends AWSCommand {
	

protected		AmazonSimpleDB mAmazon ;
	
	public AWSSDBCommand() {
		super();
	}

	protected void getSDBClient(Options opts) throws UnexpectedException {
		
			
		mAmazon =  new AmazonSimpleDBClient(
				new AWSPropertyCredentials( mShell, opts  ) 
		
		);
	}
	

	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
    }

	protected void writeAttribute(Attribute attr) throws XMLStreamException {
		startElement("attribute");
		attribute("name" , attr.getName());
		characters( attr.getValue());
		endElement();
	}

	protected void writeAttributes(List<Attribute> attributes) throws XMLStreamException {
		for( Attribute a : attributes ){
			writeAttribute(a);
			
		}
		
	}
	


}

//
//
// Copyright (C) 2008-2013    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
