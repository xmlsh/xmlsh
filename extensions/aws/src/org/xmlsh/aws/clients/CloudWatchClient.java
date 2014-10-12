package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public final class CloudWatchClient extends AWSClient<AmazonCloudWatchClient> {

	public CloudWatchClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonCloudWatchClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);
	}

}