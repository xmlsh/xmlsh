package org.xmlsh.core;

import java.net.URL;

public class ScriptSource {
	public String mScriptName;
	public URL mScriptURL;
	public String mScriptBody;
	public String mEncoding;

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