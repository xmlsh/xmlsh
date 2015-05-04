package org.xmlsh.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;


public class sqsCreateQueue extends AWSSQSCommand {

	



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("t=timeout:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() != 1 ){
			usage();
			return 1;
		}
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		int timeout = opts.getOptInt("t", 30);
		String name = args.get(0).toString();
		
		
		
		try {
			 getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = create(name , timeout );
		
		
		return ret;
		
		
	}


	private int create(String name , int timeout ) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		CreateQueueRequest request = new CreateQueueRequest();
		request.setQueueName(name);
		
		Map<String, String> attributes = new HashMap<String,String>();
		attributes.put("VisibilityTimeout", String.valueOf(timeout));
	
		
		
	
		request.setAttributes(attributes);

		traceCall("createQueue");

		CreateQueueResult result = mAmazon.createQueue(request);
		
		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		
		
			startElement("queue");
			attribute("url", result.getQueueUrl());

			endElement();
			
		
		
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		


		
		
		return 0;
		
		
		
		
	}


	

}
