package org.xmlsh.aws.util;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;

public  abstract class AWSClient<T extends AmazonWebServiceClient>  {
	protected T mClient ;
	
	public AWSClient( T client ){
		mClient = client ;
	}
	
	public T getClient( ){
		return mClient ;
	}


	public void setEndpoint( String endpoint )
	{
		mClient.setEndpoint( endpoint );
	}

	public void setRegion(Region region) {
		mClient.setRegion(region);
	}
	
	public void setRegion(String region) {
		setRegion( RegionUtils.getRegion(region));

	}

	protected void setEndpoint(Shell shell, Options opts) throws InvalidArgumentException {

		if( opts.hasOpt("endpoint") )
			setEndpoint(opts.getOptStringRequired("endpoint"));


	}
	protected void setRegion(Shell shell,Options opts){
		if( opts.hasOpt("region"))
			setRegion(opts.getOptString("region",Regions.DEFAULT_REGION.getName()) );
		else {
			String region = shell.getEnv().getVarString("AWS_REGION");
			if( Util.isBlank(region))
				region = shell.getEnv().getVarString("EC2_REGION");  // ec2 command line compatibility
			if( !Util.isBlank(region))
				setRegion(region);
		}

	}

}
