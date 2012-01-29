/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.calabash;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.NamedPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.util.Util;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XPipeline;

public class xproc extends XCommand {

	

    private XProcRuntime runtime = null;

	
	@Override
	public int run(List<XValue> args) throws Exception {
	
		/**
		 * DAL: Note can NOT share processor with Calabash, it does global things to it
		 * which are incompatible and not thread safe
		 */
      XProcConfiguration config = new XProcConfiguration(false);
      runtime = new XProcRuntime(config);
   //   runtime.setPhoneHome(false);
      
      Processor proc = config.getProcessor();
      DocumentBuilder builder =  proc.newDocumentBuilder();

		
		
		
		Hashtable<String,Vector<XdmNode>>	inputs = new Hashtable<String,Vector<XdmNode>>();
		Hashtable<QName, String> parameters = new Hashtable<QName,String>();
		Hashtable<QName, String> options = new Hashtable<QName,String>();

		Options opts = new Options("b=base:,n,iw=iwrap,ow=owrap,o=option:+");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		boolean noInput = opts.hasOpt("n");
		String base = opts.getOptString("b", null);
		
		if( args.size() != 1 )
			usage();
		
		
	    InputStream ins = getInput( args.get(0)).asInputStream(getSerializeOpts());
		
		XdmNode spec = builder.build(new StreamSource(ins));
		ins.close();
		if( base != null )
			spec.getUnderlyingNode().setSystemId(base);
		
		boolean iwrap = opts.hasOpt("iw");
		boolean owrap = opts.hasOpt("ow");
		
		OptionValue ovs = opts.getOpt("o");
		if( ovs != null ){
			for( XValue v : ovs.getValues() ){
				String value = v.toString();
				String[] namevalue = value.split("=");
				if( namevalue.length != 2 )
					usage();
				options.put(new QName(namevalue[0]), namevalue[1]);
				
				
				
			}
			
		}
		
		
		
		
		if( ! noInput ){
			
			InputStream in = getStdin().asInputStream(getSerializeOpts());

			XdmNode root = builder.build(new StreamSource(in));
			Vector<XdmNode> sources  = getSources( root , iwrap );
			inputs.put("source", sources );
		}
		
		for( NamedPort<InputPort> input : getEnv().getInputPorts() ){
			String name = input.getName();
			if( ! Util.isBlank(name) && ! "input".equals(name)){
				InputStream in = input.getPort().asInputStream(getSerializeOpts());

				XdmNode root = builder.build(new StreamSource(in));
				Vector<XdmNode> sources  = getSources( root , iwrap );
				inputs.put( name , sources );
			}
		}
		
		
		
		Hashtable<String,ReadablePipe> results = runPipe(spec, inputs,  parameters, options );
		
		
		writeResults(results,owrap);
		
		
		
		
		
		return 0;
		
		
	}



	/*
	 * Get the sources, optionally unwrapping from root document
	 * 
	 */


	private Vector<XdmNode> getSources(XdmNode root, boolean iwrap) throws SaxonApiException {
		Vector<XdmNode> sources = new Vector<XdmNode>();
		if( ! iwrap ){
			sources.add( root );
			return sources;
		}
		
		// wrap each sub-node in a document
		XQueryCompiler compiler = runtime.getProcessor().newXQueryCompiler();
	
		XQueryExecutable exec = compiler.compile( "for $node in /*/* return document { $node } " );
		
		XQueryEvaluator eval = exec.load();
		eval.setContextItem( root  );
		for( XdmItem item : eval ){
			if( item instanceof XdmNode )
				sources.add( (XdmNode) item );
			
			
		}
		return sources;

	}




	private void writeResults(Hashtable<String, ReadablePipe> results, boolean owrap ) throws SaxonApiException,
			CoreException, IOException 
	{
		ReadablePipe pipe = results.get("result");
		if( pipe != null )
			writeResult(pipe, getStdout(),owrap);
		
		
		for( String portname : results.keySet() ){
			if( portname.equals("result"))
				continue;
			if( portname.equals("error")){
				writeResult( results.get(portname), getStderr(),false);
				continue ;
			}
			
			OutputPort out = getEnv().getOutputPort( portname );
			if( out != null )
				writeResult( results.get(portname) , out,owrap );
				
			
		}
		
	}




	private void writeResult(ReadablePipe pipe,OutputPort port, boolean owrap) 
	throws SaxonApiException, CoreException,IOException {
		
		XdmNode result = null;
		if( owrap )
			result = wrapResult( pipe );
		else
			result = 	pipe.read();
		
		
		
		/*
		Destination out = port.asDestination(getSerializeOpts());
		writeXdmValue(result, out);
		*/
		
		
		   Processor qtproc = runtime.getProcessor();

           XQueryCompiler xqcomp = qtproc.newXQueryCompiler();
           XQueryExecutable xqexec = xqcomp.compile(".");
           XQueryEvaluator xqeval = xqexec.load();
           xqeval.setContextItem(result);

         //  Serializer serializer = new Serializer();
         //  serializer.setOutputStream(port.asOutputStream());

           xqeval.setDestination(port.asDestination(getSerializeOpts()));
           xqeval.run();
           
          
		/*
		  XPathCompiler compiler = runtime.getProcessor().newXPathCompiler();

		
		
			XPathExecutable exec = compiler.compile( "/document/title" );

			XPathSelector eval = exec.load();
			eval.setContextItem( result );
			XdmValue v = eval.evaluate();
			NodeInfo info = (NodeInfo) v.getUnderlyingValue();
			TinyTree.diagnosticDump(info);
			*/
		/*	
		
		Destination out = port.asDestination(getSerializeOpts());
		runtime.getProcessor().writeXdmValue(result, out);
		*/
		/*
         
		Destination out = port.asDestination(getSerializeOpts());
		writeXdmValue(result, out);
	
	*/
    
	}

	private XdmNode wrapResult(ReadablePipe pipe) throws SaxonApiException {
		
		ArrayList<XdmItem> list = new ArrayList<XdmItem>();
		do {
			list.add(pipe.read());
		} while( pipe.moreDocuments());
		
		 
		 XdmValue docs =  new XdmValue(  list  );
		
		
		
		  Processor qtproc = runtime.getProcessor();

           XQueryCompiler xqcomp = qtproc.newXQueryCompiler();
           XQueryExecutable xqexec = xqcomp.compile("declare variable $docs external; <wrap>{$docs}</wrap>");
           XQueryEvaluator xqeval = xqexec.load();
           xqeval.setExternalVariable(new QName("docs"), docs );
           

          return (XdmNode) xqeval.evaluate();
           
	}


	public  void writeXdmValue(XdmValue value, Destination destination) throws SaxonApiException {
		
		try {
	        Receiver out = destination.getReceiver(runtime.getProcessor().getUnderlyingConfiguration());
	        ComplexContentOutputter out2 = new ComplexContentOutputter();
	        out2.setPipelineConfiguration(runtime.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration());
	        out2.setReceiver(out);
	        
	        TreeReceiver tree = new TreeReceiver(out2);
	        tree.open();
	        tree.startDocument(0);
	        for (Iterator<XdmItem> it = value.iterator(); it.hasNext();) {
	            XdmItem item = it.next();
	            tree.append((Item)item.getUnderlyingValue(), 0, NodeInfo.ALL_NAMESPACES ); // NodeInfo.NO_NAMESPACES );//NodeInfo.LOCAL_NAMESPACES ); // NodeInfo.LOCAL_NAMESPACES ); // NodeInfo.NO_NAMESPACES);//NodeInfo.ALL_NAMESPACES);
	        }
	        tree.endDocument();
	        tree.close();
	    } catch (XPathException err) {
	        throw new SaxonApiException(err);
	    }
	}

	
	/**
	 * DAL: Note:
	 * This method (runPipe) was copied from the calabash source RunTestReport.java with explicit permission of the author, Norman Walsh.
	 * 
	 * @param pipeline
	 * @param inputs
	 * @param outputs
	 * @param parameters
	 * @param options
	 * @return
	 * @throws SaxonApiException
	 */

	private Hashtable<String,ReadablePipe> runPipe(XdmNode pipeline,
	                                               Hashtable<String, Vector<XdmNode>> inputs,
	                                          //     Hashtable<String, Vector<XdmNode>> outputs,
	                                               Hashtable<QName, String> parameters,
	                                               Hashtable<QName, String> options) throws SaxonApiException {

	    XPipeline xpipeline = runtime.use(pipeline);

	    if (inputs != null) {
	        for (String port : inputs.keySet()) {
	            xpipeline.clearInputs(port);
	            for (XdmNode node : inputs.get(port)) {
	                xpipeline.writeTo(port, node);
	            }
	        }
	    }

	    if (parameters != null) {
	        for (QName name : parameters.keySet()) {
	            xpipeline.setParameter(name, new RuntimeValue(parameters.get(name)));
	        }
	    }

	    if (options != null) {
	        for (QName name : options.keySet()) {

	            // HACK HACK HACK!
	            RuntimeValue v;
                v = new RuntimeValue(options.get(name));

	            xpipeline.passOption(name, v);
	        }
	    }

	    try {
	        xpipeline.run();
	    } catch (XProcException e) {
	      
	        throw e;
	    } catch (Exception e) {
	        throw new XProcException(e);
	    }

	    Hashtable<String, ReadablePipe> pipeoutputs = new Hashtable<String, ReadablePipe> ();
	    Set<String> pipeouts = xpipeline.getOutputs();
	    for (String port : pipeouts ) {

	            ReadablePipe rpipe = xpipeline.readFrom(port);
	            rpipe.canReadSequence(true);
	            pipeoutputs.put(port, rpipe);
	        
	    }

	    return pipeoutputs;
	}
	
}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
