package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

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

		Options opts = getOptions("update:,exists:,+r=replace,q=quiet:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		

		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		String updateName = opts.getOptString("update", null);
		String updateExists = opts.getOptString("exists",null);
		boolean bReplace = opts.getOptFlag("replace",true);
		
		
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
		ret = put(domain,item,args,updateName,updateExists,bReplace, opts.hasOpt("q"));

		
		
		return ret;
		
		
	}


	private int put(String domain, String item, List<XValue > args, String updateName, String updateExists, boolean bReplace, boolean bQuiet) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		
		
		UpdateCondition cond = null  ;
		if( ! Util.isEmpty(updateName))
			cond =  new UpdateCondition( updateName , updateExists , ! Util.isEmpty(updateExists)) ;
		
		
		
			List<ReplaceableAttribute> attributes = getAttributes( args , bReplace );
         
		PutAttributesRequest request = new PutAttributesRequest(domain,item,attributes,cond);
		
		
		
		traceCall("putAttributes");

		mAmazon.putAttributes(request);
		
		if( ! bQuiet ){
			OutputPort stdout = this.getStdout();
			mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
			emptyDocument();
			closeWriter();
			stdout.writeSequenceTerminator(getSerializeOpts());
		}	
		
		

		return 0;
		
		
	}


	private List<ReplaceableAttribute> getAttributes(List<XValue> args, boolean bReplace) 
	{
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		while( !args.isEmpty()){

			String name = args.remove(0).toString();
			String value = args.isEmpty() ? "" : args.remove(0).toString();
 
			
			attrs.add( 
					new ReplaceableAttribute().withName(name).withValue(value).withReplace(bReplace));
		
		}
		return attrs ;
	}



	public void usage() {
		super.usage();
	}



	

}
