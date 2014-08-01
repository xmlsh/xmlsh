package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;


public class snsSubscribe extends AWSSNSCommand {





	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("t=topic:,p=protocol:,e=endpoint-arn:");
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

		ret = subscribe( opts.getOptStringRequired("topic"),
				opts.getOptStringRequired("protocol"),
				opts.getOptStringRequired("endpoint-arn"));


		return ret;


	}


	private int subscribe(String arn, String protocol, String endpoint ) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		SubscribeRequest request = new SubscribeRequest(arn,protocol,endpoint);
		traceCall("subscribe");

		SubscribeResult result = mAmazon.subscribe(request);

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());


		startDocument();
		startElement(getName());


		startElement("subscription");
		attribute("arn",result.getSubscriptionArn());

		endElement();



		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;




	}




}
