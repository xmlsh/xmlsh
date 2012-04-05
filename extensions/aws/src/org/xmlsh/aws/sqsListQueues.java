package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;


public class sqsListQueues extends AWSSQSCommand {

	

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
		if( args.size() > 0 )
			prefix = args.get(0).toString();
		
		
		
		try {
			mAmazon = getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = list(prefix);
		
		
		return ret;
		
		
	}


	private int list(String prefix) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		
		ListQueuesRequest request = new ListQueuesRequest();
		if( prefix != null )
			request.setQueueNamePrefix(prefix);
		
		ListQueuesResult result = mAmazon.listQueues(request);
		for( String  url : result.getQueueUrls() ){
			startElement("queue");
			attribute("url", url);
			
			
			
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