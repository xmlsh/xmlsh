package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;

import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.ListSubscriptionsRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsResult;
import com.amazonaws.services.sns.model.Subscription;


public class snsListSubscriptions extends AWSSNSCommand {



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));




		try {
			getSNSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret;

		if( args.size() == 0 )
			ret = list();
		else
			ret = list(args.get(0).toString());


		return ret;


	}


	private int list() throws IOException, XMLStreamException, SaxonApiException, CoreException {


		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());


		startDocument();
		startElement(getName());

		traceCall("listSubscriptions");

		ListSubscriptionsResult result = getAWSClient().listSubscriptions();

		do {
			for( Subscription subscription : result.getSubscriptions()){
				startElement("subscription");
				attribute("endpoint",subscription.getEndpoint());
				attribute("owner",subscription.getOwner());
				attribute("protocol",subscription.getProtocol());
				attribute("subscription-arn",subscription.getSubscriptionArn());
				attribute("topic-arn",subscription.getTopicArn());

				endElement();

			}
			if( result.getNextToken() != null )
				result = getAWSClient().listSubscriptions( new ListSubscriptionsRequest().withNextToken(result.getNextToken()));

		} while(result.getNextToken() != null );



		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}

	private int list(String topic) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());


		startDocument();
		startElement(getName());


		ListSubscriptionsByTopicResult result = getAWSClient().listSubscriptionsByTopic(new ListSubscriptionsByTopicRequest(topic));

		do {
			for( Subscription subscription : result.getSubscriptions()){
				startElement("subscription");
				attribute("endpoint",subscription.getEndpoint());
				attribute("owner",subscription.getOwner());
				attribute("protocol",subscription.getProtocol());
				attribute("subscription-arn",subscription.getSubscriptionArn());
				attribute("topic-arn",subscription.getTopicArn());

				endElement();

			}
			if( result.getNextToken() != null )
				result = getAWSClient().listSubscriptionsByTopic( new ListSubscriptionsByTopicRequest(topic,result.getNextToken()));

		} while(result.getNextToken() != null );



		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());


		return 0;




	}


}
