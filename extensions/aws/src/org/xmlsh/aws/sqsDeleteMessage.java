package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;


public class sqsDeleteMessage extends AWSSQSCommand {

	



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() != 2 ){
			usage();
			return 1;
		}
		

		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		
		
		
		
		try {
			 getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		ret = delete( args.get(0).toString() , args.get(1).toString() );
		
		
		return ret;
		
		
	}


	private int delete(String url , String message) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		
		
		DeleteMessageRequest request = new DeleteMessageRequest();
		request.setQueueUrl(url);
		request.setReceiptHandle(message);

		traceCall("deleteMessage");

		mAmazon.deleteMessage(request);
		
				
		
		return 0;
		
		
		
		
	}


	

}
