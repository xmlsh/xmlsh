package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.ec2.AmazonEC2Client;

public final class EC2Client extends AWSClient<AmazonEC2Client> {

	public EC2Client(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonEC2Client(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);
	}
	
}