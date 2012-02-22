package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;


public class snsListTopics extends AWSSNSCommand {

	

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
		

		
		
		try {
			mAmazon = getSNSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = list();
		
		
		return ret;
		
		
	}


	private int list() throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		

		
		ListTopicsResult result = mAmazon.listTopics();
		do {
			for( Topic topic : result.getTopics()){
				startElement("topic");
				attribute("arn", topic.getTopicArn());
				endElement();
				
			}
			if( result.getNextToken() != null )
				result = mAmazon.listTopics( new ListTopicsRequest().withNextToken(result.getNextToken()));
			
			
		} while( result.getNextToken() != null );
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		

		
		
		return 0;
		
		
		
		
	}


	

}
