package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSMonCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StatisticSet;


public class monPutData	 extends  AWSMonCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("n=namespace:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		String namespace = opts.getOptStringRequired("namespace");
		
		
		try {
			 getMonClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		

		int ret = -1;
		ret = put(namespace,args);

		
		
		return ret;
		
		
	}


	

	private int put(String namespace , List<XValue> metrics ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		
		startDocument();
		startElement(getName());
		
		String nextToken = null ;
			
			Collection<MetricDatum> datumList = new ArrayList<MetricDatum >();
			for( XValue xm : metrics )
				datumList.add( parseMetric(xm));
			
			
			
			
			PutMetricDataRequest request = new PutMetricDataRequest().
					withNamespace(namespace).withMetricData(datumList);
			
			mAmazon.putMetricData(request);
	         
			



		endElement();
		endDocument();
		
		
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		
		return 0;
		
		
		
		
	}
	private MetricDatum parseMetric(XValue xv) throws UnexpectedException {
		

		return new MetricDatum().
				withDimensions( parseDimensions(xv) ).
				withMetricName(parseName(xv)).
				withStatisticValues(parseStatistics(xv)).
				withUnit(parseUnit(xv)).withValue(parseValue(xv));
		
	}

	private double parseValue(XValue xv) throws UnexpectedException {
		return xv.xpath( mShell , "xs:double(@value)").toDouble();
	}




	private String parseUnit(XValue xv) throws UnexpectedException {
		return xv.xpath( mShell, "xs:string(@unit)" ).toString();
	}




	private StatisticSet parseStatistics(XValue xv) {
		// TODO Auto-generated method stub
		return null;
	}




	private String parseName(XValue xv) throws UnexpectedException {
		return xv.xpath( mShell, "xs:string(@name)" ).toString();
	}




	private Collection<Dimension>  parseDimensions(XValue xv) throws UnexpectedException {

		List<Dimension> list = new ArrayList<Dimension>();
		
	    for( String nv : xv.xpath(mShell, "xs:string(@dimensions)").toString().split(",") ){
	    	StringPair pair = new StringPair( nv , '=');
	    	list.add( new Dimension().withName(pair.getLeft()).withValue(pair.getRight()));
	    	
	    	
	    }
	    return list ;

		
	}




	public void usage() {
		super.usage();
	}



	

}
