/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

public class xtee extends XCommand {

	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options(SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		// List of outputs to tee to
		List<XMLEventWriter> writers = new ArrayList<XMLEventWriter>();

		InputPort stdin = getStdin();

		XMLEventReader reader = null;
		try {
			SerializeOpts sopts = getSerializeOpts(opts);
			reader = stdin.asXMLEventReader(sopts);
			OutputPort stdout = getStdout();

			writers.add(stdout.asXMLEventWriter(sopts));

			for (XValue arg : args) {
				OutputPort output = getEnv().getOutput(arg, false);
				writers.add(output.asXMLEventWriter(sopts));
			}

			stdout.setSystemId(stdin.getSystemId());
			XMLEvent e;

			while (reader.hasNext()) {
				e = (XMLEvent) reader.next();
				for (XMLEventWriter writer : writers)
					writer.add(e);
			}

		} finally {
			Util.safeClose(reader);
			for (XMLEventWriter writer : writers)
				Util.safeClose(writer);
		}
		return 0;

	}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
