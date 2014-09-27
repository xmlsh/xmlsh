package org.xmlsh.aws.util;



import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;

public class AWSCommandCredentialsProviderChain extends AWSCredentialsProviderChain
{
	static String getDefaultProfileName(Shell shell) {
		String profile = shell.getEnv().getVarString("AWS_DEFAULT_PROFILE"); // AWS CLI env var for default profile
		if( Util.isBlank(profile))
			profile = ProfilesConfigFile.DEFAULT_PROFILE_NAME;
		else
			// AWS CLI compatible
			profile = "profile " + profile ;
		return profile ;
	}

	public AWSCommandCredentialsProviderChain(Shell shell , Options opts)  {
		super(
				new AWSOptionsCredentialsProvider( shell , opts),
				new AWSEnvCredentialsProvider(shell),
				new EnvironmentVariableCredentialsProvider(),
				new ProfileCredentialsProvider(getDefaultProfileName(shell)),
				new SystemPropertiesCredentialsProvider(),
				new InstanceProfileCredentialsProvider());
		   
		   

	}





}