package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.log;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.sh.core.SourceLocation;

// The 'Static Context' (similar to XQuery/XSLT)
// Composed of Functions, Modules, Namespaces anything which is determined
// at parse/compile time inherited by the shell
// All components of ModuleContext are immutable and sharable at any one one
// time,
// but the static context itself is not imutable - it is affected by parse
// and runtime declarations
public class StaticContext implements Cloneable, Closeable {
	private static Logger mLogger = LogManager.getLogger();
	private FunctionDefinitions mFunctions = null;
	private Modules mModules;                    // Imported modules visible to this module
	private int id = _id++;
	private static int _id = 0; 
	private boolean bClosed = false ;
	private		Namespaces	mNamespaces = null;
	
	
	// log debugging
	public String toString() {
		return "CTX: " + id ;
	}
	
	public StaticContext()
	{
		mLogger.entry();


		
	}
	

	public StaticContext clone()
	{
		mLogger.entry(this);
		
		return mLogger.exit(new StaticContext( this ));
	}

	
	// Clone this context 
	protected StaticContext(StaticContext that) {
		mLogger.entry(that);
		
		if( that.mFunctions != null)
		    mFunctions = that.mFunctions.clone();
		
		if( that.mModules != null )
			mModules = that.mModules.clone() ;
		if( this.mNamespaces != null )	
			that.mNamespaces = new Namespaces( this.mNamespaces );
		
		mLogger.exit();
		
	}
	public void declareFunction(IFunctionDecl func) {
		mLogger.entry(func);

		if (mFunctions == null)
			mFunctions = new FunctionDefinitions();
		mFunctions.put(func.getName(), func);
	}


	public IFunctionDecl getFunctionDecl(String name) {
		if (mFunctions == null)
			return null;
		return mFunctions.get(name);
	}

	private FunctionDefinitions getFunctions() {
		return mFunctions;
	}

	public FunctionDefinitions getFunctions(boolean bCreate) {
		if (mFunctions == null && bCreate )
			mFunctions = new FunctionDefinitions();
		return mFunctions;
	}

	Modules getModules() {
		return mModules;
	}

	public Modules getModules(boolean bCreate) {
		if( mModules == null && bCreate )
			mModules = new Modules();
		return mModules;
		
	}

	@Override
	public void close() throws IOException {
		mLogger.entry();
		if( bClosed )
			return;
		if( mModules != null )
			mModules.release();
		if( mFunctions != null )
			mFunctions.clear();
		
		mModules = null;
		mFunctions = null ;
		bClosed = true ;
		
	}
	public Namespaces getNamespaces()
	{
	  if( mNamespaces == null )
      mNamespaces = new Namespaces();
		return mNamespaces;
	}

	public Iterable<ModuleHandle> getDefaultModules() {
		List<ModuleHandle> all = new ArrayList<>();
		for( ModuleHandle mh : mModules ){
			
			String uri = Shell.toModuleUri(mh);
			if( ! getNamespaces().containsValue( uri ) )
				all.add(mh );
		}
		
		return mLogger.exit( all );
		
		
	}

}