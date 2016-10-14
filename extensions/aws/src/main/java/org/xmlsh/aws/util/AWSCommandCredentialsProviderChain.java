package org.xmlsh.aws.util;

import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import lombok.Data;

public class AWSCommandCredentialsProviderChain
    extends AWSCredentialsProviderChain
{

  public AWSCommandCredentialsProviderChain(Shell shell, Options opts) {
    this(  new AWSOptionsCredentialsProvider(shell, opts)  , shell , opts  );
  }

  private AWSCommandCredentialsProviderChain(
   AWSOptionsCredentialsProvider optProvider,
      Shell shell, Options opts) {
  super( optProvider,
  new AWSEnvCredentialsProvider(shell),
  new EnvironmentVariableCredentialsProvider(),
  new ProfileCredentialsProvider(optProvider.getProfileName()),
  new SystemPropertiesCredentialsProvider(),
  new InstanceProfileCredentialsProvider()  );
  }
}