package org.xmlsh.aws;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;


public class sqsCreateQueue extends AWSSQSCommand {





	private static final String QUEUE_ARN = "QueueArn";


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("t=timeout:");
        parseOptions(opts, args);
        setSerializeOpts(this.getSerializeOpts(opts));

		args = opts.getRemainingArgs();

		if( args.size() != 1 ){
			usage();
			return 1;
		}






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

		CreateQueueResult result = getAWSClient().createQueue(request);


		List<String> qa = Collections.singletonList(QUEUE_ARN);


		GetQueueAttributesResult aresult = getAWSClient().getQueueAttributes( result.getQueueUrl() ,qa );



		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());


		startDocument();
		startElement(getName());

		startElement("queue");
		attribute("url", result.getQueueUrl());
		attribute("arn", aresult.getAttributes().get(QUEUE_ARN));
		endElement();
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;




	}




}
