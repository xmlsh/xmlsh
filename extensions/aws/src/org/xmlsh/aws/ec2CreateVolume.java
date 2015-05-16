package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.VolumeType;


public class ec2CreateVolume extends AWSEC2Command {

	
	private static Logger mLogger = LogManager.getLogger(ec2CreateVolume.class);
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("t=type:,s=size:,zone=availability-zone:,iops:,snapshot=snapshot-id:,spec=volspec:,+encrypted");
		opts.parse(args);

		args = opts.getRemainingArgs();
        setSerializeOpts(this.getSerializeOpts(opts));

		rateRetry = opts.getOptInt("rate-retry", 0);
		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
		int ret = create( opts );
		return ret;
		
		
	}


	private int create(Options opts ) throws IOException, XMLStreamException, SaxonApiException, InterruptedException, CoreException  
	{
	
		
		CreateVolumeRequest request = new CreateVolumeRequest()
		   .withAvailabilityZone(opts.getOptStringRequired("zone"))
		   .withVolumeType(VolumeType.Gp2); // Default to GP2 
		
		
		 /**
	     * The volume type. This can be <code>gp2</code> for General Purpose
	     * (SSD) volumes, <code>io1</code> for Provisioned IOPS (SSD) volumes, or
	     * <code>standard</code> for Magnetic volumes. <p>Default:
	     * <code>standard</code>
	     * <p>
	     * <b>Constraints:</b><br/>
	     * <b>Allowed Values: </b>standard, io1, gp2
	     */
		

		if(opts.hasOpt("spec"))
			parseVolumeSpec( opts.getOptStringRequired("spec") , request );
		
 		// Overrides 
		

		if(opts.hasOpt("type"))
			request.setVolumeType(opts.getOptStringRequired("type"));
		if( opts.hasOpt("size"))
			request.setSize( opts.getOptInt("size", 10));
		if( opts.hasOpt("snapshot"))
			request.setSnapshotId(opts.getOptStringRequired("snapshot"));
		if( opts.hasOpt("iops"))
			request.setIops(opts.getOptInt("iops", 100));
		if( opts.hasOpt("encrypted"))
			request.setEncrypted(opts.getOptFlag("encrypted", false));
		
		traceCall("createVolume");

		CreateVolumeResult result = null ;
		
		int retry = rateRetry ;
		int delay = retryDelay ;
		do {
			try {
	            result=	mAmazon.createVolume(request);
	            break ;
		
			} catch( AmazonServiceException e ){
				mShell.printErr("AmazonServiceException" , e );
				if( retry > 0 && Util.isEqual("RequestLimitExceeded",e.getErrorCode())){
					mShell.printErr("AWS RequestLimitExceeded - sleeping " + delay );
					Thread.sleep( delay );
					retry--;
					delay *= 2 ;
					
					
				}
				else
					throw e;
				
			}
		} while( retry > 0 );
		
		
		writeResult(result);

		return 0;
	}
	
// 	[snapshot-id]:[volume-size]:[delete-on-termination]:[volume-type[:iops]]:[encrypted

	private void parseVolumeSpec(String spec,
			CreateVolumeRequest request) throws InvalidArgumentException {
		
	// Parse out the EBS stuff
	// [snapshot-id]:[volume-size]:[volume-type[:iops]]:[encrypted]


	String aebs[] = spec.split(":");


	// [snapshot-id]:
	if( aebs.length >= 1 ){
		String snapshotId = aebs[0];
		if( ! Util.isBlank(snapshotId))
			request.setSnapshotId(snapshotId);

	}
	// :[volume-size]:
	if( aebs.length >= 2 ){
		if( !Util.isBlank(aebs[1]))
			request.setSize( new Integer( aebs[1]));

	}
	
	//  [delete-on-termination] ignored for create-volume but keep placeholder
	if( aebs.length >=  3 ){
		if( !Util.isBlank(aebs[2]))
			printErr("delete-on-termination flag ignored for dynamicly created  volumes");
			// ebs.setDeleteOnTermination( Boolean.valueOf( Util.parseBoolean(aebs[2])));
	}
	if( aebs.length >= 4 ){
		// [volume-type[:iops]]:[encrypted]
		int i = 3;
		if( !Util.isBlank(aebs[i])){
			request.setVolumeType(aebs[i]);
			if( aebs[i].equals( VolumeType.Io1.toString())) {
				i++ ;
				if( aebs.length  <= i || Util.isBlank(aebs[i]) )
					throw new InvalidArgumentException("EBS block mapping with VolumeType io1 MUST have PIOPS");
				request.setIops( Integer.valueOf( aebs[i]));
			}
			i++;
			if( aebs.length > i && ! Util.isBlank(aebs[i]))
				request.setEncrypted( Util.parseBoolean(aebs[i]));
		}
	}

}

	private	void writeResult(CreateVolumeResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
		

	    startResult();

		writeVolume( result.getVolume() );
		endResult();

	}




}
