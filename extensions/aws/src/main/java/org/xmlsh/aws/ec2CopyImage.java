package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.ec2.model.CopyImageRequest;
import com.amazonaws.services.ec2.model.CopyImageResult;


public class ec2CopyImage extends AWSEC2Command {




	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


        Options opts = getOptions("r=source-region:,s=source-ami-id:,n=name:,d=description:,c=client-token:");
        parseOptions(opts, args);


        setSerializeOpts(this.getSerializeOpts(opts));
        args = opts.getRemainingArgs();



		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret = copy( opts );





		return ret;


	}




	private int copy( Options opts ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		String sourceImageId = opts.getOptStringRequired("source-ami-id");
		String sourceRegion = opts.getOptStringRequired("source-region");

		CopyImageRequest  request =( new CopyImageRequest()).withSourceImageId(sourceImageId)
				.withSourceRegion(sourceRegion);

		String name = opts.getOptString("name", null);
		String desc = opts.getOptString("description", null);
		if( name != null )
			request.setName(name);
		if( desc != null )
			request.setDescription(desc);


		traceCall("copyImage");

		CopyImageResult result = getAWSClient().copyImage(request);
		writeResult(result);
		return 0;



	}




	private void writeResult(CopyImageResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException {

        startResult();


        startElement("image");
        attribute("image-id", result.getImageId());
        endElement();

        endResult();
	}





}
