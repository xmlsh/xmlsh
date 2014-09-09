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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
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
	private SourceMode mSourceMode;
	private IModule mModule;
	private SourceLocation mLocation;
	private ScriptSource mSource;


	// Finalize script command make sure to close
	@Override
	protected void finalize()
	{
		close();
	}


	/*
	public ScriptCommand( File script, SourceMode sourceMode , SourceLocation location) throws FileNotFoundException, MalformedURLException
	{
		mScriptURL = script.toURI().toURL();
		mScriptName =  name == null ?  FileUtils.toJavaPath(script.getPath()) : name ;
		mSourceMode = sourceMode;
		mLocation = location ;
	}
	*/

	public ScriptCommand( SourceMode sourceMode, SourceLocation location, IModule module , ScriptSource source ) throws FileNotFoundException
	{
		mSource = source;
		mSourceMode = sourceMode;
		mLocation = location ;
		mModule = module ;

	}
	

	/*
	 * Script from literal string (eval)
	 */
	public ScriptCommand( SourceMode sourceMode , IModule module,  ScriptSource source ) throws UnsupportedEncodingException
	{
		mSource = source ;
		mSourceMode = sourceMode ;
		mModule = module ;

	}


	@Override
	public int run(Shell shell, String cmd, List<XValue> args) throws ThrowException, ParseException, IOException, UnimplementedException {

		try ( Reader mScriptStreamSource = getScriptSource() ){
			
		  switch( mSourceMode ){
			case SOURCE :
				return shell.runScript(mScriptStreamSource,mSource.mScriptName,true).mExitStatus;
			case RUN :
			{
				Shell sh = shell.clone();
				try {
					if( args != null )
						sh.setArgs(args);
					sh.setArg0(mSource.mScriptName);
					int ret = sh.runScript(mScriptStreamSource,mSource.mScriptName,true).mExitStatus;

					return ret;
				} finally {
					// Close shell - even if exception is thrown through sh.runScript and up
					sh.close();
				}
			}
			case VALIDATE: 
			  
		    return shell.validateScript( mScriptStreamSource , mSource.mScriptName ) ? 0 : 1 ;

			case IMPORT : 
			{
          int ret =  shell.runScript( mScriptStreamSource, mSource.mScriptName, true ).mExitStatus;;
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


	private Reader getScriptSource() throws IOException {
		if( mSource.mScriptURL != null )
		  return new InputStreamReader(mSource.mScriptURL.openStream(), mSource.mEncoding );
		if(mSource.mScriptBody != null )
			return Util.toReader(mSource.mScriptBody);
		throw new IOException("Script body is empty");
			
	}

	@Override
	public void close() {

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_SCRIPT ;
	}

	@Override
	public URL getURL() {
		return mSource.mScriptURL ; // may be null 

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
		return mSource.mScriptName;
	}


  @Override
  public void print(PrintWriter w, boolean bExec)
  {
    w.print( mSource.mScriptName );
  }



}
