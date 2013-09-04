package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sqs.model.DeleteQueueRequest;


public class sqsDeleteQueue extends AWSSQSCommand {

	



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() != 1 ){
			usage();
			return 1;
		}
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		String name = args.get(0).toString();
		
		
		
		try {
			 getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = delete(name  );
		
		
		return ret;
		
		
	}


	private int delete(String name  ) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		DeleteQueueRequest request = new DeleteQueueRequest();
		request.setQueueUrl(name);

		traceCall("deleteQueue");

		mAmazon.deleteQueue(request);
		
		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		
		
			startElement("queue");
			attribute("url",name);

			endElement();
			
		
		
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		


		
		
		return 0;
		
		
		
		
	}


	

}
