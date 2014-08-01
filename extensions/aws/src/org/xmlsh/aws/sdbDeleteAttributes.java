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

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.UpdateCondition;


public class sdbDeleteAttributes	 extends  AWSSDBCommand {



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("update:,exists:,q=quiet");
		opts.parse(args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));

		String updateName = opts.getOptString("update", null);
		String updateExists = opts.getOptString("exists",null);


		try {
			getSDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		if( args.size() < 2){
			usage(getName()+ ":" + "domain item attributes ...");

		}
		String domain = args.remove(0).toString();
		String item   = args.remove(0).toString();


		int ret = -1;
		ret = delete(domain,item,args,updateName,updateExists, opts.hasOpt("q"));



		return ret;


	}


	private int delete(String domain, String item, List<XValue > args, String updateName, String updateExists, boolean bQuiet) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{



		UpdateCondition cond = null  ;
		if( ! Util.isEmpty(updateName))
			cond =  new UpdateCondition( updateName , updateExists , ! Util.isEmpty(updateExists)) ;




		List<Attribute> attributes = getAttributes( args );

		DeleteAttributesRequest request = new DeleteAttributesRequest(domain,item).withAttributes(attributes).withExpected(cond);

		traceCall("deleteAttributes");

		mAmazon.deleteAttributes(request);

		if( ! bQuiet ){
			OutputPort stdout = this.getStdout();
			mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

			emptyDocument();

			closeWriter();
			stdout.writeSequenceTerminator(getSerializeOpts());

		}


		return 0;


	}


	private List<Attribute> getAttributes(List<XValue> args) 
	{
		List<Attribute> attrs = new ArrayList<Attribute>(args.size());
		while( !args.isEmpty()){

			String name = args.remove(0).toString();
			attrs.add( new Attribute(name,null) );



		}
		return attrs ;
	}



	@Override
	public void usage() {
		super.usage();
	}





}
