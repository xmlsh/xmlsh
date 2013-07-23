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

import com.amazonaws.services.ec2.model.LaunchPermission;
import com.amazonaws.services.ec2.model.LaunchPermissionModifications;
import com.amazonaws.services.ec2.model.ModifyImageAttributeRequest;

public class ec2ModifyImageAttribute extends AWSEC2Command {

	




	/**
	 * @param args
	 * @throws IOException 
	 * 
	 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("p=product-codes,l=launch,a=add:+,r=remove:+");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		
		
		if( args.size() != 1 ){
			usage(null);
			return 1;
		}
		

		mSerializeOpts = this.getSerializeOpts(opts);
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		boolean bAdd = opts.hasOpt("add") || ! opts.hasOpt("remove");
		
		
		int ret = modifyImage( args.get(0).toString(), bAdd, opts );
		
		
		
		
		
		return ret;	
	}

	private int modifyImage( String image_id, boolean bAdd, Options opts ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		String attribute	 = opts.hasOpt("launch") ? "launchPermission" : "productCodes" ;
		
		
		ModifyImageAttributeRequest request = new ModifyImageAttributeRequest( image_id, attribute  );

		request.setOperationType(bAdd ? "add" : "remove");
		if( opts.hasOpt("launch"))
		    // request.setLaunchPermission(getLaunchPermissions(bAdd,opts));
			request.setUserIds(getUserIds(opts,bAdd));
		else
		if( opts.hasOpt("product-codes")){
			request.setProductCodes( getProductCodes( opts, bAdd ) );
			request.setUserIds(null);
			request.setLaunchPermission(null);
		}
		else {
			usage(null);
			return 1;
		}
		
	
		traceCall("modifyImageAttribute");
		mAmazon.modifyImageAttribute(request);

		writeResult( image_id );
		
		
		return 0;
	}

	private Collection<String> getUserIds(Options opts,boolean bAdd ) throws InvalidArgumentException {
		return parseStrings(opts.getOptValues(bAdd ? "add" : "remove") );
	}

	private Collection<String> getProductCodes(Options opts,boolean bAdd ) throws InvalidArgumentException {

		Collection<String> codes = parseStrings( opts.getOptValues(bAdd ? "add" : "remove") );

		return codes ;
		
	}

	private Collection<String> parseStrings(List<XValue> values) {
		Collection<String> p = new ArrayList<String>();
		for( XValue xv : values ){
			for( String vl : xv.asStringList() ){
				for( String s : vl.split(",") ){
					p.add(s);
				}
			}
		}
		return p;
	}

	private LaunchPermissionModifications getLaunchPermissions(boolean bAdd, Options opts) throws InvalidArgumentException {
		LaunchPermissionModifications launch = new LaunchPermissionModifications();

		
		
		Collection<LaunchPermission> perms = parseLaunchPermissions( opts.getOptValues("launch") );
		
		if( bAdd )
	       launch.setAdd(perms);	 		
		else
	       launch.setRemove(perms);	 		
		
		return launch;
	}

	private Collection<LaunchPermission> parseLaunchPermissions(List<XValue> values) {
		
		Collection<LaunchPermission> p = new ArrayList<LaunchPermission>();
		for( XValue xv : values ){
			for( String vl : xv.asStringList() ){
				for( String s : vl.split(",") ){
					if( s.equals("all"))
					    p.add( new LaunchPermission().withGroup(s));
					else
					    p.add( new LaunchPermission().withUserId(s));
				}
			}
		}
		return p;
	}

	private void writeResult(String image_id) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		startElement("image");
		attribute( "image-id", image_id);
		endElement();
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		
	}

	
	

}
