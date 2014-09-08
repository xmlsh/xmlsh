/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.FlagOption;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.StringOption;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class rngvalidate extends XCommand {

	public rngvalidate() {
		// TODO Auto-generated constructor stub
	}

	static private String usageKey = "usage";

	static public void setUsageKey(String key) {
		usageKey = key;
	}

	private boolean timing = false;
	private String encoding = null;
	private Localizer localizer = new Localizer(rngvalidate.class);

	@Override
	public int run(List<XValue> args) throws Exception {

		ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);

		Options opts = new Options("i,t,c,d,f,e:,p:,s,C:");
		opts.parse(args);

		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, eh);
		RngProperty.CHECK_ID_IDREF.add(properties);
		SchemaReader sr = null;
		boolean compact = false;
		boolean outputSimplifiedSchema = false;
		List<String> catalogUris = new ArrayList<String>();

		if (opts.hasOpt("i"))
			properties.put(RngProperty.CHECK_ID_IDREF, null);
		if (opts.hasOpt("C"))
			catalogUris.add(getEnv().getShell().getURL(opts.getOptStringRequired("C")).toString());
		if (opts.hasOpt("c"))
			compact = true;

		if (opts.hasOpt("d")) {
			if (sr == null)
				sr = new AutoSchemaReader();
			FlagOption option = (FlagOption) sr.getOption(SchemaReader.BASE_URI + "diagnose");
			if (option == null) {
				eh.print(localizer.message("no_schematron", "d"));
				return 2;
			}
			properties.put(option.getPropertyId(), Flag.PRESENT);
		}
		if (opts.hasOpt("t"))
			timing = true;

		if (opts.hasOpt("e"))
			encoding = opts.getOptStringRequired("e");

		if (opts.hasOpt("f"))
			RngProperty.FEASIBLE.add(properties);

		if (opts.hasOpt("s"))
			outputSimplifiedSchema = true;

		if (opts.hasOpt("p")) {
			if (sr == null)
				sr = new AutoSchemaReader();
			StringOption option = (StringOption) sr.getOption(SchemaReader.BASE_URI + "phase");
			if (option == null) {
				eh.print(localizer.message("no_schematron", "p"));
				return 2;
			}
			properties.put(option.getPropertyId(), opts.getOptStringRequired("p"));
		}

		if (!catalogUris.isEmpty()) {
			try {
				properties.put(ValidateProperty.RESOLVER, new CatalogResolver(catalogUris));
			} catch (LinkageError e) {
				eh.print(localizer.message("resolver_not_found"));
				return 2;
			}
		}
		if (compact)
			sr = CompactSchemaReader.getInstance();

		args = opts.getRemainingArgs();

		if (args.size() < 1) {
			// eh.print(localizer.message(usageKey, Version.getVersion(Driver.class)));
			return 2;
		}
		long startTime = System.currentTimeMillis();
		long loadedPatternTime = -1;
		boolean hadError = false;
		try {
			ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), sr);
			InputSource in = getEnv().getInputSource(args.remove(0), this.getSerializeOpts());
			if (encoding != null)
				in.setEncoding(encoding);
			if (driver.loadSchema(in)) {
				loadedPatternTime = System.currentTimeMillis();
				if (outputSimplifiedSchema) {
					String simplifiedSchema = driver.getSchemaProperties().get(
							RngProperty.SIMPLIFIED_SCHEMA);
					if (simplifiedSchema == null) {
						eh.print(localizer.message("no_simplified_schema"));
						hadError = true;
					} else
						System.out.print(simplifiedSchema);
				}
				if( args.size() == 0 ) {// stdin
					if (!driver.validate( getEnv().getStdin().asInputSource(getSerializeOpts())))
						hadError = true;

				} else {
					for (XValue arg : args ){
						if (!driver.validate(getEnv().getInputSource(arg, getSerializeOpts())))
							hadError = true;
					}
				}
			} else
				hadError = true;
		} catch (SAXException e) {
			hadError = true;
			eh.printException(e);
		} catch (IOException e) {
			hadError = true;
			eh.printException(e);
		}
		if (timing) {
			long endTime = System.currentTimeMillis();
			if (loadedPatternTime < 0)
				loadedPatternTime = endTime;
			eh.print(localizer.message("elapsed_time",
					new Object[] { loadedPatternTime - startTime, endTime - loadedPatternTime,
					endTime - startTime }));
		}
		if (hadError)
			return 1;
		return 0;
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
