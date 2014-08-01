package org.xmlsh.aws.util;



import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

public class AWSOptionsCredentialsProvider implements AWSCredentialsProvider
{
	private String accessKey;
	private String secretAccessKey;



	public AWSOptionsCredentialsProvider(Shell shell , Options opts) {
		super();


		// Try options first
		accessKey 		 = opts.getOptString("accessKey", null  );
		secretAccessKey  = opts.getOptString("secretKey" , null );

	}



	@Override
	public AWSCredentials getCredentials() {
		if (accessKey != null &&
				secretAccessKey != null) {

			return new BasicAWSCredentials(
					accessKey,
					secretAccessKey);
		}

		throw new AmazonClientException(
				"Unable to load AWS credentials from options " +
						"(accessKey and secretKey)");
	}



	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}	





}