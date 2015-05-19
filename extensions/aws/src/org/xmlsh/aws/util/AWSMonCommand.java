/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public abstract class AWSMonCommand extends AWSCommand {
	
	protected		AmazonCloudWatch mAmazon ;
	
	public AWSMonCommand() {
		super();
	}
	protected Object getClient() {
		return mAmazon; 
	}

	protected void getMonClient(Options opts) throws UnexpectedException, InvalidArgumentException {
		
			
		mAmazon =  new AmazonCloudWatchClient(
				new AWSCommandCredentialsProviderChain( mShell, opts  ) 
		
		);

		setEndpoint(opts);
		setRegion(opts);

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
	   @Override
	    protected String getCommonOpts() { 
	        return AWSCommand.sCOMMON_OPTS;
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
