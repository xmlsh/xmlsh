package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;


public class ec2RunInstances extends AWSEC2Command {

	




	/**
	 * @param args
	 * @throws IOException 
	 * 
	 	ec2-run-instances ami_id [-n instance_count] [-g group [-g group ...]] 
	 		[-k keypair] [-d user_data |-f user_data_file] 
	 		[-instance-type instance_type] [-availability-zone zone] [-kernel kernel_id] 
	 		[-ramdisk ramdisk_id] [-block-device-mapping block_device_mapping] [-monitor] 
	 		[-disable-api-termination] [-instance-initiated-shutdown-behavior behavior] 
	 		[-placement-group placement-group] [-tenancy tenancy] [-subnet subnet] 
	 		[-private-ip-address ip_address] [-client-token token] [-additional-info info] 

	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("n:,g=group:+,k=key:,d=user-data:,f=user-data-file:,t=instance-type:,availability-zone:,kernel:,ramdisk:,block-device-mapping:+,monitor,disable-api-termination,instance-initiated-shutdown-behavior:,placement-group:,tenancy:,subnet:,private-ip-address:,client-token:,additional-info:,tag:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		
		
		if( args.size() != 1 ){
			usage(null);
			return 1;
		}
		

		mSerializeOpts = this.getSerializeOpts(opts);
		try {
			mAmazon = getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret = runInstance( args.get(0).toString(), opts );
		
		
		
		
		
		return ret;	
	}

	private int runInstance( String ami_id, Options opts ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
/*
 	ec2-run-instances ami_id [-n instance_count] [-g group [-g group ...]] 
	 		[-k keypair] [-d user_data |-f user_data_file] 
	 		[-instance-type instance_type] [-availability-zone zone] [-kernel kernel_id] 
	 		[-ramdisk ramdisk_id] [-block-device-mapping block_device_mapping] [-monitor] 
	 		[-disable-api-termination] [-instance-initiated-shutdown-behavior behavior] 
	 		[-placement-group placement-group] [-tenancy tenancy] [-subnet subnet] 
	 		[-private-ip-address ip_address] [-client-token token]


 */
		
		
		
		
		
		StringPair minMax = new StringPair( opts.getOptString("n" , "1"),'-');
		
		int	   min = minMax.hasDelim() ? Util.parseInt(minMax.getLeft(), 1) : Util.parseInt( minMax.getRight(), 1);
		int	   max = minMax.hasDelim() ? Util.parseInt(minMax.getRight(), min) : min ;
		
		
		
		RunInstancesRequest request = new RunInstancesRequest(ami_id, min , max );
		
		// -g group 
		List<XValue> xgroups = opts.getOptValues("g");
		if( xgroups != null )
			request.setSecurityGroups( Util.toStringList(xgroups));
		
		
		// -k keypair
		String keypair = opts.getOptString("k", null);
		if( keypair != null )
			request.setKeyName( keypair );
		
		String userData = getUserData( opts );
		if( userData != null )
			request.setUserData(userData);
		
		String instanceType = opts.getOptString("instance-type", null);
		if( instanceType != null )
			request.setInstanceType(instanceType);
		
		Placement 	 placement = getPlacement(opts);
		if( placement != null )
			
			request.setPlacement(placement);
		
		String kernel = opts.getOptString("kernel", null);
		if( kernel != null )
			request.setKernelId(kernel);
		
		String ramdisk = opts.getOptString("ramdisk", null);
		if( ramdisk != null )
			request.setRamdiskId(ramdisk);

		Collection<BlockDeviceMapping>	blockDeviceMappings = getBlockDeviceMappings( opts );
		if( blockDeviceMappings != null )
			request.setBlockDeviceMappings(blockDeviceMappings);
		
		
		request.setMonitoring( Boolean.valueOf(opts.hasOpt("monitor")));
		request.setDisableApiTermination(Boolean.valueOf(opts.hasOpt("disable-api-termination")));
		String shutdown = opts.getOptString("instance-initiated-shutdown-behavior", null);
		if( shutdown != null )
			request.setInstanceInitiatedShutdownBehavior(shutdown);
		
		String subnet = opts.getOptString("subnet", null);
		if( subnet != null )
			request.setSubnetId(subnet);
		
		String private_ip = opts.getOptString("private-ip-address",null);
		if( private_ip != null )
			request.setPrivateIpAddress(private_ip);
		
		String client_token = opts.getOptString("client-token", null);
		if( client_token != null )
			request.setClientToken(client_token);
		String info = opts.getOptString("additional-info", null );
		if( info != null )
			request.setAdditionalInfo(info);
		
	
		RunInstancesResult  result = mAmazon.runInstances(request);

		writeResult( result );
		
		
		return 0;
	}

	private void writeResult(RunInstancesResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		writeReservation( result.getReservation() );
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		
	}

	private Placement getPlacement(Options opts) {
		Placement placement = new Placement();
		String availZone = opts.getOptString("availability-zone", null);
		if( availZone != null )
			placement.setAvailabilityZone(availZone);
		String pg = opts.getOptString("placement-group", null);
		if( pg != null )
			placement.setGroupName(pg);
		
		String tenancy = opts.getOptString("tenancy", null);
		if( tenancy != null )
			placement.setTenancy(tenancy);
		
		return placement;
			
			
			
	}

	private String getUserData(Options opts) throws IOException {
		String userData = opts.getOptString("d", null);
		if( userData != null )
			return userData ;
		
		XValue file = opts.getOptValue("f");
		if( file != null )
			return Util.readString(mShell.getFile(file), mSerializeOpts.getInputTextEncoding());
		return null;
		
		
	}

	
	/*
	 * Defines a block device mapping for the instance. This argument is passed in the form of <devicename>=<blockdevice>. 
	 * The devicename is the device name of the physical device on the instance to map. 
	 * The blockdevice can be one of the following values:

		none - specifies that the existing mapping on the specified image for this device should be suppressed. For example: /dev/sdc=none
		ephemeral[0..3] - indicates that an instance local storage device should be mapped to this device. Example: /dev/sdc=ephemeral0
		[snapshot-id]:[size]:[delete-on-termination (true|false)] - this value can be used to map a device to an existing EBS-backed volume by specifying an existing volume name. You can specify a new EBS-backed volume by skipping the snapshot ID and passing in a volume size instead; for example: /dev/sdb=:20. You can also specify whether the Amazon EBS volume should be deleted on termination; this value is true by default.

	 */
	private Collection<BlockDeviceMapping> getBlockDeviceMappings(Options opts) throws InvalidArgumentException {

		List<XValue>  blocks = opts.getOptValues("block-device-mapping");
		if( blocks == null || blocks.size() == 0 )
			return null ;
		
		
		
		
		List<BlockDeviceMapping>	mappings = new ArrayList<BlockDeviceMapping>(blocks.size());
		for( XValue b : blocks )
			mappings.add( parseBlockDeviceMapping(b.toString()));
		// TBD 
		return mappings;
		
		
		
		
	}

	private BlockDeviceMapping parseBlockDeviceMapping(String string) {
		BlockDeviceMapping map = new BlockDeviceMapping();
		StringPair 	pair = new StringPair(string , '=');
		
		String 	device = pair.getLeft();
		if( device.startsWith("/dev/"))
			device = device.substring(5);

		if(! pair.hasRight()){
			map.setNoDevice(device);
			return map;
		}
		
		
		
		String r = pair.getRight();
		if( r.equals("none")){
			map.setNoDevice(device);
			return map ;
		}
			
		map.setDeviceName(device);
		
		// Ephemeral = virtual ?
		if( ! r.contains(":")){
			map.setVirtualName(r);
			return map;
		}
		
		// Parse out the EBS stuff
		
		String aebs[] = r.split(":");
		
		EbsBlockDevice ebs = new EbsBlockDevice().withDeleteOnTermination( Boolean.FALSE );
		
		// [snapshot-id]:[size]:[delete-on-termination (true|false)]
		if( aebs.length >= 1 ){
			String snapshotId = aebs[0];
			if( ! Util.isBlank(snapshotId))
				ebs.setSnapshotId(snapshotId);

		}
		
		if( aebs.length >= 2 ){
			if( !Util.isBlank(aebs[1]))
				ebs.setVolumeSize( new Integer( aebs[1]));
			
		}
		
		if( aebs.length >=  3 ){
			if( !Util.isBlank(aebs[2]))
				ebs.setDeleteOnTermination( Boolean.valueOf( Util.parseBoolean(aebs[2])));
			
		}
		map.setEbs(ebs);
		return map;
	
	}
		

	
	

}
