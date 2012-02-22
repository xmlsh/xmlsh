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
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceLicense;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public abstract class AWSEC2Command extends AWSCommand {
	
	protected	AmazonEC2		mAmazon ;
	
	public AWSEC2Command() {
		super();
	}


	protected AmazonEC2 getEC2Client( Options opts ) throws UnexpectedException {
		return new AmazonEC2Client(
				new AWSPropertyCredentials( mShell , opts )
				);
		
	}


	protected void writeStateChages(List<InstanceStateChange> states) throws IOException, XMLStreamException,
			InvalidArgumentException, SaxonApiException {
				
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
				
				
				
			}


	private void writeStateChange(InstanceStateChange change) throws XMLStreamException {
		startElement("state_change");
		attribute( "instance_id" , change.getInstanceId() );
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
		attribute( "requestor", res.getRequesterId());
	
		 
		for( Instance inst : res.getInstances() ){
			startElement("instance");
			attribute( "instance_id" , inst.getInstanceId() );
			attribute( "image" , inst.getImageId() );
			attribute( "key_name" ,  inst.getKeyName());
			attribute( "architecture" , inst.getArchitecture());
			attribute( "client_token" , inst.getClientToken() );
			attribute( "lifecycle" , Util.notNull( inst.getInstanceLifecycle()) );
			attribute( "instance_type" , inst.getInstanceType());
			attribute( "kernel_id" , inst.getKernelId() );
			attribute( "platform" , inst.getPlatform() );
			attribute( "private_dns" , inst.getPrivateDnsName());
			attribute( "private_ip" , inst.getPrivateIpAddress() );
			attribute( "public_dns" , inst.getPublicDnsName());
			attribute( "public_ip" , inst.getPublicIpAddress() );
			attribute( "ramdisk_id" , inst.getRamdiskId());
			attribute( "root_device_name" , inst.getRootDeviceName() );
			
			attribute( "spot_request_id" , inst.getSpotInstanceRequestId() );
			attribute( "state_transistion_reason" , inst.getStateTransitionReason() );
			attribute( "subnet_id" , inst.getSubnetId() );
			attribute( "state" , inst.getState().getName() );
			attribute( "virtualization_type" , inst.getVirtualizationType() );
			attribute( "vpcid" , inst.getVpcId() );
			attribute( "ami_launch_index" , inst.getAmiLaunchIndex().toString() );
			
			attribute( "launch_date" , Util.formatXSDateTime(inst.getLaunchTime()) );
			attribute( "license" , getLicense(inst) );
			attribute( "monitoring" , inst.getMonitoring().getState() );
			attribute( "source_dest_check" , getSourceCheck(inst) );
			attribute( "state_reason" , getStateReason(inst) );
			
	
			writeProductCodes( inst.getProductCodes() );
			writeTags( inst.getTags() );
			
			
			writePlacement(inst.getPlacement());
			
			
			startElement("devices");
			for( InstanceBlockDeviceMapping device : inst.getBlockDeviceMappings() ){
				writeBlockMapping(mWriter, device);
	
				
			}
			endElement();
			
			
			endElement();
			
		}
		
		endElement();
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
	
	public void writeProductCodes(List<ProductCode> productCodes) throws XMLStreamException {
		if( productCodes == null )
			return ;
		startElement("product_codes");
		for( ProductCode code : productCodes ){
			startElement("product_code");
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
		attribute( "availability_zone" , placement.getAvailabilityZone() );
		attribute( "group_name" , placement.getGroupName());
		attribute("tenancy", placement.getTenancy() );
		endElement();
	}


	public void writeBlockMapping(XMLStreamWriter writer, InstanceBlockDeviceMapping device)
			throws XMLStreamException {
		startElement("device");
		attribute("name", device.getDeviceName());
		
		EbsInstanceBlockDevice ebs = device.getEbs();
		attribute( "status" , ebs.getStatus() );
		attribute("volume_id", ebs.getVolumeId());
		attribute("attach_date" , Util.formatXSDateTime(ebs.getAttachTime()));
		attribute("delete_on_termination", ebs.getDeleteOnTermination().toString());
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
		attribute( "delete_on_termination", AWSUtil.parseBoolean(attachment.getDeleteOnTermination()));
		attribute( "attach_date", Util.formatXSDateTime(attachment.getAttachTime()));
		attribute( "device", attachment.getDevice());
		attribute( "instance_id", attachment.getInstanceId() );
		attribute( "volume_id", attachment.getVolumeId());
		endElement();
	}


	protected Collection<String> getIps(List<XValue> args) throws UnknownHostException {
		ArrayList<String> 	ips = new ArrayList<String>( args.size() );
		for( String sip : Util.toStringArray( args)){
			ips.add( AWSUtil.resolveDNS(sip));
			
		}
		return ips;
		
		
	}



}

//
//
// Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
