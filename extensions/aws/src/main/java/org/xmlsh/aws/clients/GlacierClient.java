package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.glacier.AmazonGlacierClient;

public final class GlacierClient extends AWSClient<AmazonGlacierClient> {

	public GlacierClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonGlacierClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);

	}

}