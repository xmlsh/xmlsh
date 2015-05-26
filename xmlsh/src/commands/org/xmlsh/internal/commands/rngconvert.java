/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.util.Util;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.MultiInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.translate.Formats;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;



public class rngconvert extends XCommand {




	private final ErrorHandlerImpl eh = new ErrorHandlerImpl();
	private String inputType;
	private String outputType;
	private static final String DEFAULT_OUTPUT_ENCODING = ShellConstants.kENCODING_UTF_8;
	private static final int DEFAULT_LINE_LENGTH = 72;
	private static final int DEFAULT_INDENT = 2;


	@Override
	public int run(List<XValue> args) throws Exception {
		List<String> catalogUris = new ArrayList<String>();
		String[] inputParamArray = null; 
		String[] outputParamArray = null; 

		Options opts = new Options("C:,I:,O:,i:,o:");
		opts.parse(args);


		if (opts.hasOpt("C"))
			catalogUris.add(getEnv().getShell().getURL(opts.getOptStringRequired("C")).toString());

		inputType = opts.getOptString("I",null);
		outputType = opts.getOptString("O",null);

		if( opts.hasOpt("i")){
			inputParamArray = Util.toStringArray(opts.getOptValues("i")); 

		} else
			inputParamArray = new String[0];


		if( opts.hasOpt("o")){
			outputParamArray = Util.toStringArray(opts.getOptValues("o")); 
		} else
			outputParamArray = new String[0];

		args = opts.getRemainingArgs();


		if (inputType == null) {
			inputType = extension(args.get(0).toString());
			if (inputType.length() > 0)
				inputType = inputType.substring(1);
		}


		final InputFormat inputFormat = Formats.createInputFormat(inputType);
		if (inputFormat == null) 
			throw new InvalidArgumentException("Unrecognized input type: " + inputType );

		String ext = extension(args.get(args.size() - 1).toString());
		if (outputType == null) {
			outputType = ext;
			if (outputType.length() > 0)
				outputType = outputType.substring(1);
		}
		final OutputFormat outputFormat = Formats.createOutputFormat(outputType);
		if (outputFormat == null) 
			throw new InvalidArgumentException("Unrecognized output type: " + outputType );


		Resolver resolver;
		if (catalogUris.isEmpty())
			resolver = null;
		else {
			resolver = new CatalogResolver(catalogUris);
		}



		outputType = outputType.toLowerCase();
		SchemaCollection sc;
		if (args.size() > 2 ) {
			if (!(inputFormat instanceof MultiInputFormat)) {
				throw new InvalidArgumentException("Too many arguments");
			}
			String[] uris = new String[args.size() - 1];
			for (int i = 0; i < uris.length; i++)
				uris[i] = getAbsoluteURI(args.get(i).toString());

			sc = ((MultiInputFormat)inputFormat).load(uris, inputParamArray, outputType, eh, resolver);
		}
		else
			sc = inputFormat.load(getAbsoluteURI(args.get(0).toString()), inputParamArray, outputType, eh, resolver);
		if (ext.length() == 0)
			ext = outputType;
		OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(),
				getFile(args.get(args.size()- 1)),
				ext,
				DEFAULT_OUTPUT_ENCODING,
				DEFAULT_LINE_LENGTH,
				DEFAULT_INDENT);
		outputFormat.output(sc, od, outputParamArray, inputType.toLowerCase(), eh);
		return 0;


	}

	private void error(String message) {
		eh.printException(new SAXException(message));
	}

	static private String extension(String s) {
		int dot = s.lastIndexOf(".");
		if (dot < 0)
			return "";
		return s.substring(dot);
	}


}

//
//
//Copyright (C) 2008-2014 David A. Lee.
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
