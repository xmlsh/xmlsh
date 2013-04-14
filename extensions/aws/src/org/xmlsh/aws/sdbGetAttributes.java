package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;


public class sdbGetAttributes	 extends  AWSSDBCommand {


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("c=consistant");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		boolean bConsistant = opts.hasOpt("consistant");
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		
		
		try {
			 getSDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		if( args.size() !=2 ){
			usage(getName()+ " domain item");
			
		}
		String domain = args.remove(0).toString();
		String item = args.remove(0).toString();
		

		int ret = -1;
		ret = getAttributes(domain,item,bConsistant);

		
		
		return ret;
		
		
	}


	private int getAttributes( String domainName, String itemName , boolean bConsistantRead ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		
		
		
		startDocument();
		startElement(getName());


		GetAttributesRequest getAttributesRequest = 
				new GetAttributesRequest( domainName, itemName ).withConsistentRead(bConsistantRead);
		
		
		GetAttributesResult result = mAmazon.getAttributes(getAttributesRequest);

		startElement("item");
		attribute("name",itemName);
		writeAttributes(result.getAttributes());
		endElement();
			
			
		endElement();
		endDocument();
		
		
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		return 0;
		
		
	}



	public void usage() {
		super.usage();
	}



	

}
