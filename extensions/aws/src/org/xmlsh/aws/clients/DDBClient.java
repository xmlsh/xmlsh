package org.xmlsh.aws.clients;


import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public final class DDBClient extends AWSClient<AmazonDynamoDBClient> {

	private String mRegion = null;
	private String mEndpoint = null ;

	public DDBClient(Shell shell,Options opts) throws InvalidArgumentException {
		super(	new AmazonDynamoDBClient(new AWSCommandCredentialsProviderChain( shell , opts ) ));


		if( Util.isEqual(mRegion,"local") && mEndpoint != null )
			getClient().setEndpoint( mEndpoint , "dynamodb" , mRegion );
		else {
			if( mRegion != null )
				setRegion(RegionUtils.getRegion(mRegion));
			if( mEndpoint != null )
				setEndpoint( mEndpoint );

		}


	}
	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
		mRegion = region;
	}

	@Override
	public void setEndpoint( String endpoint )
	{
		mEndpoint = endpoint ;
	}

}