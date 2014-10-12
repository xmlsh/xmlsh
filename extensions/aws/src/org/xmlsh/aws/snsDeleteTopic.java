package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sns.model.DeleteTopicRequest;


public class snsDeleteTopic extends AWSSNSCommand {





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



		setSerializeOpts(this.getSerializeOpts(opts));




		String name = args.get(0).toString();



		try {
			getSNSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret;

		ret = delete(name );


		return ret;


	}


	private int delete(String name ) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {


		DeleteTopicRequest request = new DeleteTopicRequest();
		request.setTopicArn(name);
		traceCall("deleteTopic");

		getAWSClient().deleteTopic(request);



		return 0;




	}




}
