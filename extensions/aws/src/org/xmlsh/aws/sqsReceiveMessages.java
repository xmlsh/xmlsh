package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;


public class sqsReceiveMessages extends AWSSQSCommand {

	



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("f=file:,m=max:,t=timeout:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() < 1 ){
			usage();
			return 1;
		}
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		String url = args.get(0).toString();
		OutputPort	out ;
		
		int max = opts.getOptInt("m", 1);
		int visibilityTimeout = opts.getOptInt("timeout", 0);
		
		
		// Get message from file 
		if( opts.hasOpt("f")){
			out = mShell.getEnv().getOutput(opts.getOptValue("f"),false);
			
		} else
		if( args.size() == 1 )
			out = getStdout();
		else
		{
			usage();
			return 1 ;
		}
		
		
			
		
		try {
			mAmazon = getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		
		ret = receive(url ,  max , visibilityTimeout,   out );
		
		
		return ret;
		
		
	}



	private int receive(String url, int max ,  int visibilityTimeout , OutputPort out ) throws IOException, InvalidArgumentException, XMLStreamException, SaxonApiException  {
		

		List<String> attributeNames = new ArrayList<String>(1);
		attributeNames.add("All");
		
		ReceiveMessageRequest request = new ReceiveMessageRequest();
		request.setQueueUrl(url);
		request.setAttributeNames(attributeNames);
		request.setMaxNumberOfMessages(max);
		if( visibilityTimeout != 0 )
			request.setVisibilityTimeout(visibilityTimeout);
		
		
		
		ReceiveMessageResult result = mAmazon.receiveMessage(request);
		
		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		for( Message m :  result.getMessages() ){
		
			startElement("message");
			attribute("md5", m.getMD5OfBody() );
			attribute("id" , m.getMessageId());
			attribute("receipt" , m.getReceiptHandle() );
			startElement("attributes");
			for( Entry<String,String> attr : m.getAttributes().entrySet() ){
				startElement("attribute");
				attribute( "name" , attr.getKey() );
				attribute("value", attr.getValue() );
				endElement();
				
			}
			endElement();
			startElement("body");
			mWriter.writeCharacters( m.getBody() );
			endElement();

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
