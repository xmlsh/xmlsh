package org.xmlsh.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;


public class sqsSetQueueAttributes extends AWSSQSCommand {





	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
        parseOptions(opts, args);
        setSerializeOpts(this.getSerializeOpts(opts));

		args = opts.getRemainingArgs();

		if( args.size() < 2 ){
			usage();
			return 1;
		}

		String name = args.remove(0).toString();






		try {
			getSQSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret;



		ret = setAttributes( name , parseAttributes( Util.toStringList(args) ) );


		return ret;

	}


	private Map<String, String> parseAttributes(List<String> list) {

		Map<String,String> map = new HashMap<String,String>(list.size());

		for( int i = 0 ; i < list.size() ; ){
			map.put( list.get(i), list.get(i+1));
			i+=2;
		}
		return map ;
	}


	private int setAttributes(String name , Map<String,String> attrs ) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {

		SetQueueAttributesRequest request = new SetQueueAttributesRequest(name,attrs);
		traceCall("setQueueAttributes");
		getAWSClient().setQueueAttributes(request);
		return 0;
	}




}
