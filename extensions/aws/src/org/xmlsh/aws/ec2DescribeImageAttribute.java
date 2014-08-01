package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.DescribeImageAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeImageAttributeResult;
import com.amazonaws.services.ec2.model.ImageAttribute;
import com.amazonaws.services.ec2.model.ProductCode;


public class ec2DescribeImageAttribute extends AWSEC2Command {

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("p=product-codes,l=launch");
		opts.parse(args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));



		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}




		if( args.size() != 1 ){
			usage(null);
			return 1;
		}

		int ret = describe( args.get(0).toString() , opts );



		return ret;


	}


	private int describe(String ami_id ,Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


		startDocument();
		startElement(this.getName());

		String attribute	 = opts.hasOpt("launch") ? "launchPermission" : "productCodes" ;

		DescribeImageAttributeRequest  request = new DescribeImageAttributeRequest(ami_id , attribute);

		traceCall("describeImageAttribute");

		DescribeImageAttributeResult result = mAmazon.describeImageAttribute(request);



		writeImageAttribute(result.getImageAttribute());





		endElement();
		endDocument();
		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}




	private void writeImageAttribute(ImageAttribute imageAttribute) throws XMLStreamException {
		startElement("image-attribute");
		attribute("image-id", imageAttribute.getImageId());

		writeLaunchPermissions(imageAttribute.getLaunchPermissions());

		List<ProductCode> codes = imageAttribute.getProductCodes();
		writeProductCodes(codes);
		List<BlockDeviceMapping> deviceMappings = imageAttribute.getBlockDeviceMappings();
		writeBlockDeviceMappings(deviceMappings);


		endElement();

	}


	@Override
	public void usage() {
		super.usage("Usage: ec2-describe-images [options] [image-id]");
	}






}
