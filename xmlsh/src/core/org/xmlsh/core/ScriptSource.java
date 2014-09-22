package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.module.Resource;
import org.xmlsh.sh.module.ResourceID;
import org.xmlsh.util.Util;

public class ScriptSource  extends Resource {
	public String mScriptBody;
	public String mEncoding;
	
	static Logger mLogger = LogManager.getLogger();
	


	public ScriptSource(String scriptName, URL scriptURL, String encoding) throws URISyntaxException {
		super( new ResourceID( scriptName, scriptURL.toURI()) , scriptURL );
		mEncoding = encoding;
	}

	public ScriptSource(String scriptName, String scriptBody) {
		super( new ResourceID( scriptName ) ,null );
		mScriptBody = scriptBody;
	}

	public String getScriptName() {
		return super.getName();
	}

	public Reader openReader() throws UnsupportedEncodingException, IOException {
		if (!isOpaque() )
			return new InputStreamReader(super.getURL().openStream(), mEncoding);
		else
		if (mScriptBody != null)
			return Util.toReader(mScriptBody);
		else
			throw new IOException("Script body is empty");
	}

	

}