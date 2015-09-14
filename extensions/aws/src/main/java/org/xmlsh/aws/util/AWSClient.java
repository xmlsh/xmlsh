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
	// region and endpoint settings are exclusive
	protected boolean hasRegionOrEndpoint = false;

	public AWSClient( T client ){
		mClient = client ;
	}

	public T getClient( ){
		return mClient ;
	}


	public void setEndpoint( String endpoint )
	{
	    if( hasRegionOrEndpoint )
            throw new IllegalArgumentException("Region or endpoint previously set");
		mClient.setEndpoint( endpoint );
	}

	public void setRegion(Region region) {
	    if( hasRegionOrEndpoint )
          throw new IllegalArgumentException("Region or endpoint previously set");
		mClient.setRegion(region);
		hasRegionOrEndpoint=true ;
	}

	public void setRegion(String region) {
	       if( hasRegionOrEndpoint )
	           throw new IllegalArgumentException("Region or endpoint previously set");
		setRegion( RegionUtils.getRegion(region));

	}

	protected void setEndpoint(Shell shell, Options opts) throws InvalidArgumentException {


		if( opts.hasOpt("endpoint") )
			setEndpoint(opts.getOptStringRequired("endpoint"));


	}
	protected void setRegion(Shell shell,Options opts) throws InvalidArgumentException{
		if( opts.hasOpt("region"))
			setRegion(opts.getOptStringRequired("region" ));

	 else {
		    // AWS CLI uses "AWS_DEFAULT_REGION as well as --region and the config profile region
		    // Java SDK does not
		    if( ! hasRegionOrEndpoint ){
			  String region = shell.getEnv().getVarString("AWS_DEFAULT_REGION");
			  if( !Util.isBlank(region))
				  setRegion(region);
		    }
		}

	}

}
