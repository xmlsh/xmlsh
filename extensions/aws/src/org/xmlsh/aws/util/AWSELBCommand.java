/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.Instance;

public abstract class AWSELBCommand extends AWSCommand {
	

	protected		AmazonElasticLoadBalancingClient mAmazon ;
	
	public AWSELBCommand() {
		super();
	}

	protected void getELBClient(Options opts) throws UnexpectedException {
		
			
		mAmazon =  new AmazonElasticLoadBalancingClient(
				new AWSCommandCredentialsProviderChain( mShell, opts  ) 
		
		);
	}

	protected List<Instance> instances(List<XValue> args) {
		List<Instance> li = new ArrayList<Instance>(args.size());
		for( XValue a : args )
			li.add( new Instance( a.toString()));
		return li;
				
	}

	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
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
