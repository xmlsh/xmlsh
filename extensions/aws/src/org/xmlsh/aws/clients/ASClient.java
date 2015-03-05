package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.regions.Region;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;

public final class ASClient extends AWSClient<AmazonAutoScalingClient> {

	public ASClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonAutoScalingClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);
	}
	
	@Override
	public void setEndpoint( String endpoint )
	{
		mClient.setEndpoint( endpoint );
	}

	@Override
	public void setRegion(Region region) {
		mClient.setRegion(region);
	}
/*
	
	public boolean createLaunchConfig(){
	    mClient.
	      createLaunchConfiguration(
	         new  CreateLaunchConfigurationRequest().
	           withKeyName("Name").
	              withImageId("ami-eb14341") );
	      
	}
*/
}
