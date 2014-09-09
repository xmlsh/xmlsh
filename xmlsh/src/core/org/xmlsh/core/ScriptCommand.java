/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

public class ScriptCommand implements ICommand {
  
  public enum SourceMode {
    SOURCE ,
    RUN , 
    IMPORT ,
    VALIDATE 
  };
  
  

	private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger();
	private String	mScriptName;
	private InputStream mScript;
	private SourceMode mSourceMode;
	private File	 mScriptFile; // file for script, may be null if internal script
	private IModule mModule;
	private SourceLocation mLocation;


	// Finalize script command make sure to close
	@Override
	protected void finalize()
	{
		close();
	}


	public ScriptCommand( File script, SourceMode sourceMode , SourceLocation location) throws FileNotFoundException
	{
		mScript = new FileInputStream(script);
		mScriptName = FileUtils.toJavaPath(script.getPath());
		mSourceMode = sourceMode;
		mScriptFile = script;
		mLocation = location ;

	}

	public ScriptCommand( String script , SerializeOpts opts, SourceMode sourceMode ) throws UnsupportedEncodingException
	{
		mScript = Util.toInputStream(script, opts );
		mSourceMode = sourceMode ;

	}

	public ScriptCommand(String name , InputStream is, SourceMode sourceMode, IModule module ) {
		mScriptName = FileUtils.toJavaPath(name);
		mScript = is;
		mSourceMode = sourceMode;
		mModule = module ;

	}

	@Override
	public int run(Shell shell, String cmd, List<XValue> args) throws ThrowException, ParseException, IOException, UnimplementedException {

		try {
		  switch( mSourceMode ){
			case SOURCE :
				return shell.runScript(mScript,mScriptName,true);
			case RUN :
			{
				Shell sh = shell.clone();
				try {
					if( args != null )
						sh.setArgs(args);
					sh.setArg0(mScriptName);
					int ret = sh.runScript(mScript,mScriptName,true);

					return ret;
				} finally {
					// Close shell - even if exception is thrown through sh.runScript and up
					sh.close();
				}
			}
			case VALIDATE: 
			  
		    return shell.validateScript( mScript , mScriptName ) ? 0 : 1 ;

			case IMPORT : 
			{
          int ret =  shell.runScript( mScript, mScriptName, true );
          return ret ;
			}
			  
			
		default :
		  mLogger.warn("Run mode not implemented: {}" , mSourceMode );
		  throw new UnimplementedException("Source mode: " + mSourceMode.toString() + " Not implemented");
			}
		} finally {
			close();
		}
	}

	@Override
	public void close() {
		if( mScript != null ){
			try {
				mScript.close();
			} catch (IOException e) {
				mLogger.warn("Exception closing script" , e );
			}
			mScript = null ;
		}

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_SCRIPT ;
	}

	@Override
	public File getFile() {
		return mScriptFile ; // may be null 

	}

	@Override
	public IModule getModule() {
		return mModule ;
	}

	@Override
	public SourceLocation getLocation() {
		return mLocation ;
	}

	@Override
	public void setLocation(SourceLocation loc) {
		mLocation = loc ;

	}


	public String getScriptName() {
		return mScriptName;
	}


  @Override
  public void print(PrintWriter w, boolean bExec)
  {
    w.print( mScriptName );
  }



}
