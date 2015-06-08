package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;

public final class SNSClient extends AWSClient<AmazonSNSClient> {

	public SNSClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonSNSClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
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
}