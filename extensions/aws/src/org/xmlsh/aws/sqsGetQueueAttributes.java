package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;


public class sqsGetQueueAttributes extends AWSSQSCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		String prefix = null ;
		if( args.size() < 2 ){
			usage();
			return 1;
		}
		
		
		String name = args.remove(0).toString();
		
		
		
		try {
			mAmazon = getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = list(name , Util.toStringList(args) );		
		
		return ret;
		
		
	}


	private int list(String name , List<String> attrNames ) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		

		
		GetQueueAttributesRequest request = new GetQueueAttributesRequest(name);
		request.setAttributeNames(attrNames);
		
		GetQueueAttributesResult attrsResult = mAmazon.getQueueAttributes(request);
		for( Entry<String, String>  attr : attrsResult.getAttributes().entrySet() ){
			startElement("attribute");
			attribute("name" , attr.getKey() );
			characters( attr.getValue());
			endElement();
		}
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		

		
		
		return 0;
		
		
		
		
	}


	

}
