package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.UpdateCondition;


public class sdbPutAttributes	 extends  AWSSDBCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("update:,exists:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		String updateName = opts.getOptString("update", null);
		String updateExists = opts.getOptString("exists",null);
		
		
		
		
		
		
		
		try {
			 getSDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		if( args.size() < 3 ){
			usage(getName()+ ":" + "domain item attributes ...");
			
		}
		String domain = args.remove(0).toString();
		String item   = args.remove(0).toString();
		

		int ret = -1;
		ret = put(domain,item,args,updateName,updateExists);

		
		
		return ret;
		
		
	}


	private int put(String domain, String item, List<XValue > args, String updateName, String updateExists) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		UpdateCondition cond = null  ;
		if( ! Util.isEmpty(updateName))
			cond =  new UpdateCondition( updateName , updateExists , ! Util.isEmpty(updateExists)) ;
		
		
		
		startDocument();
		startElement(getName());
		
		List<ReplaceableAttribute> attributes = getAttributes( args );
         
		PutAttributesRequest request = new PutAttributesRequest(domain,item,attributes,cond);
		
		
		
		mAmazon.putAttributes(request);
		
		
		endElement();
		endDocument();
		
		
				
		
		
		
		
		
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		return 0;
		
		
	}


	private List<ReplaceableAttribute> getAttributes(List<XValue> args) 
	{
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		while( !args.isEmpty()){

			String name = args.remove(0).toString();
			String value = args.isEmpty() ? "" : args.remove(0).toString();
 
			
			attrs.add( 
					new ReplaceableAttribute().withName(name).withValue(value));
		

		
		}
		return attrs ;
	}



	public void usage() {
		super.usage();
	}



	

}
