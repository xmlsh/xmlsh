package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.simpledb.AmazonSimpleDBClient;

public final class SDBClient extends AWSClient<AmazonSimpleDBClient> {

	public SDBClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonSimpleDBClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));
		setRegion(shell,opts);
		setEndpoint(shell,opts);

	}

}