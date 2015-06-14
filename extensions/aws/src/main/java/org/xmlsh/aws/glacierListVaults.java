package org.xmlsh.aws;

import java.io.IOException;
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

import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;


public class glacierListVaults	 extends  AWSGlacierCommand {



	private List<InstanceState> states;
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
			getGlacierClient(opts);
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


		ListVaultsRequest request = new ListVaultsRequest();


		startDocument();
		startElement(getName());

		traceCall("listVaults");

		ListVaultsResult result = getAWSClient().listVaults(request);

		for( DescribeVaultOutput vault  : result.getVaultList() ){
			startElement("valut");
			attribute("creation-date" , vault.getCreationDate());
			attribute("last-inventory-date" ,vault.getLastInventoryDate());
			attribute("number-of-archives" ,vault.getNumberOfArchives());
			attribute("size" ,vault.getSizeInBytes());
			attribute("arn" ,vault.getVaultARN());
			attribute("name" ,vault.getVaultName());
			endElement();

		}
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
