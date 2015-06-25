package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.aws.util.AWSGlacierCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;

import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;


public class glacierPutArchive	 extends  AWSGlacierCommand {



	private List<InstanceState> states;
	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
        parseOptions(opts, args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));


		if( args.size() <3)
			usage();

		String vault = args.remove(0).toString();
		String desc = args.remove(0).toString();




		try {
			getGlacierClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = -1;
		ret = put(vault,desc, args , opts );



		return ret;


	}


	private int put(String vault, String desc, List<XValue> files, Options opts ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

		ArchiveTransferManager tm = new ArchiveTransferManager(getAWSClient(),  new AWSCommandCredentialsProviderChain( getShell() , opts ));



		startDocument();
		startElement(getName());

		for( XValue xf : files ){

			File file = mShell.getFile(xf);

			traceCall("ArchiveTransferManager.upload");

			UploadResult result = tm.upload(vault, desc, file);
			startElement("upload");
			attribute("valut", vault);
			attribute("description" , desc );
			attribute("file" , file.getAbsolutePath());
			attribute("archive-id" , result.getArchiveId());
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
