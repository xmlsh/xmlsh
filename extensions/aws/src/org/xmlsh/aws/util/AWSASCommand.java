/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;

public abstract class AWSASCommand extends AWSCommand {
	
	protected	AmazonAutoScaling		mAmazon ;
	
	public AWSASCommand() {
		super();
	}

	protected Object getClient() {
		return mAmazon; 
	}

	protected void getASClient( Options opts ) throws UnexpectedException, IllegalArgumentException, InvalidArgumentException {
		mAmazon = new AmazonAutoScalingClient(
				new AWSCommandCredentialsProviderChain( mShell , opts )
				);

		setRegion(opts);
		setEndpoint(opts);		
		
	}
	


	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
    }


	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
	    mAmazon.setRegion( RegionUtils.getRegion(region));
		
	}


	/* (non-Javadoc)
	 * @see org.xmlsh.core.XCommand#run(java.util.List)
	 */
	@Override
	public int run(List<XValue> args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	

	 


}

//
//
// Copyright (C) 2008-2014    David A. Lee.
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
