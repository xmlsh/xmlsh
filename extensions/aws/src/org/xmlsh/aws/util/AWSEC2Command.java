/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceLicense;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.LaunchPermission;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;

public abstract class AWSEC2Command extends AWSCommand {
	
	protected	AmazonEC2		mAmazon ;
	


	
	
	public AWSEC2Command() {
		super();
	}
	protected Object getClient() {
		return mAmazon; 
	}


	protected void getEC2Client( Options opts ) throws UnexpectedException, InvalidArgumentException {
		mAmazon =  new AmazonEC2Client(
				new AWSCommandCredentialsProviderChain( mShell , opts )
				);
		
		setRegion(opts);
		setEndpoint(opts);
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
	    mAmazon.setRegion( RegionUtils.getRegion(region));
		
	}
	protected void writeStateChages(List<InstanceStateChange> states) throws IOException, XMLStreamException,
			SaxonApiException, CoreException {
				
				OutputPort stdout = this.getStdout();
				mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
				
				
				startDocument();
				startElement(this.getName());
				
				for( InstanceStateChange state : states ) { 
						writeStateChange(state);
					
				}
				
				endElement();
				endDocument();
				
				closeWriter();		
				stdout.writeSequenceTerminator(mSerializeOpts);
				stdout.release();
				
				
			}


	private void writeStateChange(InstanceStateChange change) throws XMLStreamException {
		startElement("state-change");
		attribute( "instance-id" , change.getInstanceId() );
		InstanceState current = change.getCurrentState();
		InstanceState prev = change.getPreviousState();
		writeState( "current" , current );
		writeState( "previous" , prev );
	
		
		endElement();
		
		
	}


	private void writeState(String elem, InstanceState state) throws XMLStreamException {
		startElement(elem);
		attribute( "name" , state.getName()  );
		attribute( "code" , String.valueOf(state.getCode() ));
		endElement();
		
	}


	protected void writeReservation(Reservation res) throws XMLStreamException {
		startElement("reservation");
		attribute("id", res.getReservationId() );
		attribute( "requester-id", res.getRequesterId());
	
		 
		for( Instance inst : res.getInstances() ){
			startElement("instance");
			attribute( "instance-id" , inst.getInstanceId() );
			attribute( "image" , inst.getImageId() );
			attribute( "key-name" ,  inst.getKeyName());
			attribute( "architecture" , inst.getArchitecture());
			attribute( "client-token" , inst.getClientToken() );
			attribute( "lifecycle" , Util.notNull( inst.getInstanceLifecycle()) );
			attribute( "instance-type" , inst.getInstanceType());
			attribute( "kernel-id" , inst.getKernelId() );
			attribute( "platform" , inst.getPlatform() );
			attribute( "private-dns" , inst.getPrivateDnsName());
			attribute( "private-ip" , inst.getPrivateIpAddress() );
			attribute( "public-dns" , inst.getPublicDnsName());
			attribute( "public-ip" , inst.getPublicIpAddress() );
			attribute( "ramdisk-id" , inst.getRamdiskId());
			attribute( "root-device-name" , inst.getRootDeviceName() );
			
			attribute( "spot-request-id" , inst.getSpotInstanceRequestId() );
			attribute( "state-transistion-reason" , inst.getStateTransitionReason() );
			attribute( "subnet-id" , inst.getSubnetId() );
			attribute( "state" , inst.getState().getName() );
			attribute( "virtualization-type" , inst.getVirtualizationType() );
			attribute( "vpcid" , inst.getVpcId() );
			attribute( "ami-launch-index" , inst.getAmiLaunchIndex().toString() );
			
			attribute( "launch-date" , Util.formatXSDateTime(inst.getLaunchTime()) );
			attribute( "license" , getLicense(inst) );
			attribute( "monitoring" , inst.getMonitoring().getState() );
			attribute( "source-dest-check" , getSourceCheck(inst) );
			attribute( "state-reason" , getStateReason(inst) );
			
	
			writeProductCodes( inst.getProductCodes() );
			writeTags( inst.getTags() );
			
			
			writePlacement(inst.getPlacement());
			
			
			writeInstanceBlockDeviceMappings(inst.getBlockDeviceMappings());
			
			
			endElement();
			
		}
		
		endElement();
	}


	public void writeInstanceBlockDeviceMappings(List<InstanceBlockDeviceMapping> mappings) throws XMLStreamException {
		startElement("devices");
		for( InstanceBlockDeviceMapping device : mappings ){
			writeInstanceDeviceMapping( device);	
		}
		endElement();
	}


	public void writeProductCodes(List<ProductCode> productCodes) throws XMLStreamException {
		if( productCodes == null )
			return ;
		startElement("product-codes");
		for( ProductCode code : productCodes ){
			startElement("product-code");
			attribute( "id" , code.getProductCodeId() );
			endElement();
			
		}
		endElement();
		
	}


	public String getStateReason(Instance inst) {
		if( inst.getStateReason() != null )
			return inst.getStateReason().getCode();
		else
			return "";
	}


	public String getSourceCheck(Instance inst) {
		
		Boolean is = inst.getSourceDestCheck();
		return is == null ? "false" : is.toString();

	}


	public String getLicense(Instance inst) {
		InstanceLicense lic = inst.getLicense();
		if( lic == null )
			return "";
		else
		return lic.toString();
	}


	public void writePlacement(Placement placement)
			throws XMLStreamException {
		startElement("placement");
		attribute( "availability-zone" , placement.getAvailabilityZone() );
		attribute( "group-name" , placement.getGroupName());
		attribute("tenancy", placement.getTenancy() );
		endElement();
	}


	public void writeInstanceDeviceMapping( InstanceBlockDeviceMapping device)
			throws XMLStreamException {
		startElement("device");
		attribute("name", device.getDeviceName());
		
		EbsInstanceBlockDevice ebs = device.getEbs();
		attribute( "status" , ebs.getStatus() );
		attribute("volume-id", ebs.getVolumeId());
		attribute("attach-date" , Util.formatXSDateTime(ebs.getAttachTime()));
		attribute("delete-on-termination", ebs.getDeleteOnTermination().toString());
		endElement();
	}



	protected List<Tag> parseTags(Options opts) throws InvalidArgumentException {
		List<Tag> tags = new ArrayList<Tag>( );
		
		for( XValue xt : opts.getOptValuesRequired("t") ) {
			StringPair pair = new StringPair( xt.toString() , '=');
			
			tags.add( 
					pair.hasLeft() ?
							new Tag(pair.getLeft() , pair.getRight() )
					:
							new Tag( pair.getRight() )
			
			);
			
		}
		return tags ;
		
		
	}


	protected void writeAttachements(List<VolumeAttachment> attachments) throws XMLStreamException {
		startElement("attachments");
		for( VolumeAttachment attachment : attachments )
			writeAttachment( attachment );
		endElement();
		
	}


	protected void writeAttachment(VolumeAttachment attachment) throws XMLStreamException {
		startElement("attachment");
		attribute( "delete-on-termination", AWSUtil.parseBoolean(attachment.getDeleteOnTermination()));
		attribute( "attach-date", Util.formatXSDateTime(attachment.getAttachTime()));
		attribute( "device", attachment.getDevice());
		attribute( "instance-id", attachment.getInstanceId() );
		attribute( "volume-id", attachment.getVolumeId());
		endElement();
	}


	protected Collection<String> getIps(List<XValue> args) throws UnknownHostException {
		ArrayList<String> 	ips = new ArrayList<String>( args.size() );
		for( String sip : Util.toStringArray( args)){
			ips.add( AWSUtil.resolveDNS(sip));
			
		}
		return ips;
		
		
	}


	protected void writeBlockDeviceMappings(List<BlockDeviceMapping> deviceMappings) throws XMLStreamException {
		startElement("device-mappings");
		for( BlockDeviceMapping mapping : deviceMappings )
			writeBlockDeviceMapping(mapping);
		endElement();
		
	}


	private void writeBlockDeviceMapping(BlockDeviceMapping device) throws XMLStreamException {
		startElement("device");
		
		attribute("name", device.getDeviceName());
		
		EbsBlockDevice ebs = device.getEbs();
		if( ebs != null ){
		
			attribute( "shapshot-id" , ebs.getSnapshotId() );
			attribute( "delete-on-termination" , ebs.getDeleteOnTermination());
			attribute("volume-size" , ebs.getVolumeSize() );
		}
		
		
		endElement();
		
	}


	public void attribute(String localName, boolean flag) throws XMLStreamException {
		attribute( localName , flag ? "true" : "false" );
		
	}

	public void writeTags(List<Tag> tags) throws XMLStreamException {
		if( tags == null )
			return ;
		startElement("tags");
		
		for(Tag tag :  tags){
			startElement("tag");
			attribute( "key" , tag.getKey());
			attribute("value", tag.getValue());
			endElement();
		}
		endElement();
		
	}


	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
    }


	protected void writeLaunchPermissions(List<LaunchPermission> launchPermissions) throws XMLStreamException {
		startElement("launch-permissions");

		for( LaunchPermission p : launchPermissions ){
			writeLaunchPermission( p );
			
		}
		endElement();
		
	}


	private void writeLaunchPermission(LaunchPermission p) throws XMLStreamException {
		startElement("launch-permissions");

		if( ! Util.isBlank(p.getGroup()))
			attribute( "group" , p.getGroup() );
		if( ! Util.isBlank(p.getUserId()))
			attribute( "user-id" , p.getUserId());
		endElement();
		
	}


	protected void writeVolume(Volume volume) throws XMLStreamException {
		startElement("volume");
		
		
		
		attribute("volume-id" , volume.getVolumeId() );
		attribute( "snapshot-id " , volume.getSnapshotId() );
		attribute( "availability-zone", volume.getAvailabilityZone());
		attribute( "create-date", Util.formatXSDateTime(volume.getCreateTime()));
		attribute( "size", volume.getSize().toString());
		attribute( "state" , volume.getState());
		writeAttachements( volume.getAttachments());
		writeTags( volume.getTags());
	}
	

}

//
//
// Copyright (C) 2008-2013    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
