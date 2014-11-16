package org.xmlsh.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSGlacierCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;

import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ListVaultsRequest;


public class glacierInventoryVault	 extends  AWSGlacierCommand {



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		if( args.size() != 1 )
			usage();



		setSerializeOpts(this.getSerializeOpts(opts));




		String vault = args.get(0).toString();

		try {
			getGlacierClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = -1;
		ret = inventory(vault);



		return ret;


	}


	private int inventory(String vault) throws IOException, XMLStreamException, SaxonApiException, CoreException, InterruptedException 
	{

		OutputPort stdout = this.getStdout();


		ListVaultsRequest request = new ListVaultsRequest();




		InitiateJobRequest initiateJobRequest = new InitiateJobRequest(vault ,
				new JobParameters().withType("inventory-retrieval")
				);

		traceCall("initiateJob");

		InitiateJobResult result = getAWSClient().initiateJob(initiateJobRequest);

		DescribeJobRequest describeJobRequest = new DescribeJobRequest(vault,result.getJobId());

		String status = null;
		DescribeJobResult describeResult= null;
		do {
			traceCall("describeJob");

			describeResult = getAWSClient().describeJob(describeJobRequest);

			status = describeResult.getStatusCode();

			mShell.printOut(status);
			if( ! status.equals("InProgress"))
				break ;

			Thread.sleep(10*1000);


		} while( true );

		if( status.equals("Succeeded")){

			GetJobOutputRequest getJobOutputRequest= new GetJobOutputRequest(vault, result.getJobId() , null);

			traceCall("getJobOutput");

			GetJobOutputResult jobOutputResult = getAWSClient().getJobOutput(getJobOutputRequest);
			InputStream jobOutput = jobOutputResult.getBody();
			Util.copyStream(jobOutput, stdout.asOutputStream(getSerializeOpts()));
			jobOutput.close();


		}


		return 0;

	}


	@Override
	public void usage() {
		super.usage();
	}





}
