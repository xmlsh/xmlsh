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

import com.amazonaws.services.sns.model.UnsubscribeRequest;


public class snsUnsubscribe extends AWSSNSCommand {





	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("t=topic:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		String topic = opts.getOptString("topic",null);

		if( topic == null ) {
			if(args.size() != 1 ){
				usage();
				return 1;
			}
			topic = args.get(0).toString();
		}



		setSerializeOpts(this.getSerializeOpts(opts));




		try {
			getSNSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret;

		ret = unsubscribe(topic );


		return ret;


	}


	private int unsubscribe(String arn ) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		UnsubscribeRequest request = new UnsubscribeRequest(arn);
		traceCall("unsubscribe");


		getAWSClient().unsubscribe(request);

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());


		startDocument();
		startElement(getName());
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());


		return 0;




	}




}
