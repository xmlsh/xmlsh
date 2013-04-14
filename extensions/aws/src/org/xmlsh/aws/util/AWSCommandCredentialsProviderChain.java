package org.xmlsh.aws.util;



import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;

public class AWSCommandCredentialsProviderChain extends AWSCredentialsProviderChain
{

	
	public AWSCommandCredentialsProviderChain(Shell shell , Options opts)  {
	        super(
	        	  new AWSOptionsCredentialsProvider( shell , opts),
	        	  new EnvironmentVariableCredentialsProvider(),
	              new SystemPropertiesCredentialsProvider(),
	              new InstanceProfileCredentialsProvider());
		
	}



	
	
}