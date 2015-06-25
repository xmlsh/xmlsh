package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.StateReason;
import com.amazonaws.services.ec2.model.Tag;

public class ec2DescribeImages extends AWSEC2Command {

	/**
	 * @param args
	 * @throws IOException
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


        Options opts = getOptions("f=filter:+");
        parseOptions(opts, args);

        args = opts.getRemainingArgs();
        setSerializeOpts(this.getSerializeOpts(opts));

		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}


        Collection<Filter> filters = 
                opts.hasOpt("filter") ?
                        parseFilters( Util.toStringList(opts.getOptValues("filter"))) : null ;

		int ret;
		switch (args.size()) {
		case 0:
			ret = describe(null, filters);
			break;
		case 1:
			ret = describe(args, filters);
			break;

		default:
			usage();
			return 1;
		}

		return ret;

	}


    protected Collection<Filter> parseFilters(List<String> values) {
        if( values == null || values.size() == 0 )
            return null;

        ArrayList<Filter> filters = new ArrayList<Filter>(values.size());

        for( String v : values ){
            StringPair nv = new StringPair( v , '=' );
            Filter filter = new Filter().withName(nv.getLeft()).withValues( nv.getRight().split(","));
            filters.add(filter);
        }
        return filters;
    }


    private int describe(List<XValue> args, Collection<Filter> filters) throws IOException, XMLStreamException, SaxonApiException, CoreException {


        OutputPort stdout = getStdout();
        mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


        startDocument();
        startElement(getName());
		DescribeImagesRequest request = new DescribeImagesRequest();
		if (args != null) {

			request.setImageIds(Util.toStringList(args));

		}

		if (filters != null)
			request.setFilters(filters);

		traceCall("describeImages");

		DescribeImagesResult result = getAWSClient().describeImages(request);


		
		for (Image image : result.getImages())
			writeImage(image);

		endElement();
		endDocument();
		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());
		return 0;

	}

	private void writeImage(Image image) throws XMLStreamException {
		startElement("image");
		attribute("image-id", image.getImageId());
		attribute("architecture", image.getArchitecture());
		attribute("hypervisor", image.getHypervisor());

		attribute("location", image.getImageLocation());
		attribute("owner-alias", image.getImageOwnerAlias());
		attribute("type", image.getImageType());
		attribute("kernel-id", image.getKernelId());
		attribute("name", image.getName());
		attribute("owner-id", image.getOwnerId());
		attribute("platform", image.getPlatform());
		attribute("ramdisk-id", image.getRamdiskId());
		attribute("root-device-name", image.getRootDeviceName());
		attribute("root-device-type", image.getRootDeviceType());

		attribute("state", image.getState());
		attribute("virtualization-type", image.getVirtualizationType());
		attribute("public", image.getPublic() ? "true" : "false");

		List<ProductCode> codes = image.getProductCodes();
		writeProductCodes(codes);
		List<BlockDeviceMapping> deviceMappings = image
				.getBlockDeviceMappings();
		writeBlockDeviceMappings(deviceMappings);
		StateReason stateReason = image.getStateReason();
		writeStateReason(stateReason);

		List<Tag> tags = image.getTags();
		writeTags(tags);

		endElement();

	}

	private void writeStateReason(StateReason stateReason)
			throws XMLStreamException {
		if (stateReason != null) {
			startElement("state-reason");
			attribute("code", stateReason.getCode());
			attribute("message", stateReason.getMessage());
			endElement();
		}

	}

	@Override
	public void usage() {
		super.usage("Usage: ec2-describe-images [options] [image-id]");
	}

}
