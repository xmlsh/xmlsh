package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSMonCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;


public class monListMetrics	 extends  AWSMonCommand {



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
			getMonClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = -1;
		ret = list(Util.toStringList(args));



		return ret;


	}




	private int list(List<String> elbs) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());



		startDocument();
		startElement(getName());

		String nextToken = null ;
		do {

			ListMetricsRequest listMetricsRequest = new ListMetricsRequest().withNextToken(nextToken);

			traceCall("listMetrics");

			ListMetricsResult result = getAWSClient().listMetrics(listMetricsRequest);

			for( Metric metric : result.getMetrics()){ 
				startElement("metric");
				attribute("name" , metric.getMetricName());
				attribute("namespace",metric.getNamespace());
				for( Dimension dim : metric.getDimensions()){
					startElement("dimension");
					attribute("name",dim.getName());
					characters( dim.getValue());
					endElement();

				}
				endElement();



			}
			nextToken = result.getNextToken();

		} while( nextToken != null );

		endElement();
		endDocument();


		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}

	@Override
	public void usage() {
		super.usage();
	}





}
