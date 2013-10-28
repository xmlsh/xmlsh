/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmVariable;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileOutputPort;
import org.xmlsh.core.IXdmItemOutputStream;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.util.MLUtil;
import org.xmlsh.sh.shell.SerializeOpts;

public abstract class MLListDirectoryRequest extends MLQueryRequest{


	MLListDirectoryRequest( String url  ) throws InterruptedException, XPathException, InvalidArgumentException, SaxonApiException
	{
		super("Listing " + url +  " ...", QueryCache.getInstance().getQuery("listDirectoryRecurse.xquery") , 
			MLUtil.newVariable("root",url) , null );

	}
}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */