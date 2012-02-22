package org.xmlsh.aws.util;



import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.amazonaws.auth.AWSCredentials;

public class AWSPropertyCredentials implements AWSCredentials
{
    private String accessKey;
    private String secretAccessKey;
    

	
	public AWSPropertyCredentials(Shell shell , Options opts) throws UnexpectedException {
		super();
		

		// Try options first
		accessKey 		 = opts.getOptString("accessKey", null  );
		secretAccessKey  = opts.getOptString("secretKey" , null );
		
		// If not then ENV vars
		if( Util.isEmpty(accessKey))
			accessKey = getVarValueOpt(shell,"AWS_ACCESS_KEY");
		
		if( Util.isEmpty(secretAccessKey))
			secretAccessKey = getVarValueOpt(shell,"AWS_SECRET_KEY");
		
		
		if( Util.isEmpty(accessKey))
			throw new UnexpectedException( "AWS_ACCESS_KEY required");
		
		if( Util.isEmpty(secretAccessKey))
			throw new UnexpectedException( "AWS_SECRET_KEY required");
		
		
		
			
		
		
		
	}



	protected String getVarValueOpt(Shell shell, String key ) {
		XValue val = shell.getEnv().getVarValue(key );
		if( val != null && ! val.isEmpty() )
			return val.toString();
		else
			return null;
	}

	

	@Override
	public String getAWSAccessKeyId() {
		return accessKey;
	}

	@Override
	public String getAWSSecretKey() {
		return secretAccessKey;
	}
	
	
}