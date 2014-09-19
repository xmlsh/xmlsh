package org.xmlsh.core;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptSource {
	public String mScriptName;
	public URL mScriptURL;
	public String mScriptBody;
	public String mEncoding;
	
	static Logger mLogger = LogManager.getLogger();
	
	public String toString() {
		return mScriptName +  "=" + mScriptURL ;
	}

	public ScriptSource(String scriptName, URL scriptURL, String encoding) {
		mScriptName = scriptName;
		mScriptURL = scriptURL;
		mEncoding = encoding;
	}

	public ScriptSource(String scriptName, String scriptBody) {
		mScriptName = scriptName;
		mScriptBody = scriptBody;
	}

}