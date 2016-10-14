package org.xmlsh.aws.util;

import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import lombok.Getter;


public class AWSOptionsCredentialsProvider implements AWSCredentialsProvider {
  
  @Getter
  String accessKey;
  @Getter
  String secretAccessKey;
  @Getter
  String sessionToken;
  @Getter 
  String profileName;
  
  public AWSOptionsCredentialsProvider(Shell shell, Options opts) {
    super();
    // Try options first
    accessKey = opts.getOptString("accessKey", null);
    secretAccessKey = opts.getOptString("secretKey", null);
    profileName =opts.getOptString("profile",null);
    sessionToken = opts.getOptString("session-token", null);
    
  }

  @Override
  public AWSCredentials getCredentials() {
    if (accessKey != null && secretAccessKey != null) {
        if( sessionToken != null )
          return new BasicSessionCredentials(accessKey, secretAccessKey,
              sessionToken);
        return new BasicAWSCredentials(accessKey, secretAccessKey);
      }
    if( profileName != null )
      return new ProfileCredentialsProvider(profileName).getCredentials();
 

    throw new AmazonClientException(
        "Unable to load AWS credentials from options "
            + "(accessKey and secretKey)");
  }


  @Override
  public void refresh() {

  }

}