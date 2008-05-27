/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.xmlsh.sh.shell.Shell;

public class ScriptCommand implements ICommand {
	
	private String	mScriptName;
	private InputStream mScript;
	private boolean mSourceMode;
	
	public ScriptCommand( File script, boolean bSourceMode ) throws FileNotFoundException
	{
		mScript = new FileInputStream(script);
		mScriptName = script.getName();
		mSourceMode = bSourceMode;
		
	}
	
	public ScriptCommand( String script )
	{
		mScript = new StringBufferInputStream(script);
		mSourceMode = true ;
		
	}
	
	
	public int run(Shell shell, String cmd, XValue[] args) throws Exception {
		
		try {
			if( mSourceMode ){
				return shell.runScript(mScript);
			} else {
			
				Shell sh = shell.clone();
				if( args != null )
					sh.setArgs(args);
				sh.setArg0(mScriptName);
				int ret = sh.runScript(mScript);
				sh.close();
				
				return ret;
			}
		} finally {
			
			mScript.close();
		}
	}

}
