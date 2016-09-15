package org.xmlsh.modules.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;
import org.xmlsh.core.Options;


public abstract class TextLineReplaceCommand extends TextCommand {

	protected String mSearch;
	protected String mReplace;

	protected List<XValue> parseOpts(List<XValue> args) throws UnknownOption, IOException, CoreException {

		args =  super.parseOpts(args);

		requires( args.size() == 2, 
				"[-input file] [-output file] search replace");

		mSearch = args.get(0).toString();
		mReplace = args.get(1).toString();
		return args;
	}
	@Override
	protected void process() throws Exception {
		processStream();
	}
	
	
	@Override
	protected void processStream(Reader r, PrintWriter w) throws IOException {
		mLogger.entry(r,w);
		String line ;
		while(  (line = Util.readLine(r)) != null ){
			
            line = replaceLine( line );
            w.println(line);
		}
	}

	protected String replaceLine(String line) {
		mLogger.entry(line);
		return mLogger.exit(line.replace( mSearch , mReplace  ));
	}

	


}
