package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

public final class ELBClient extends AWSClient<AmazonElasticLoadBalancingClient> {

	public ELBClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonElasticLoadBalancingClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);

	}

}