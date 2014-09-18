package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.Namespaces;

// The 'Static Context' (similar to XQuery/XSLT)
// Composed of Functions, Modules, Namespaces anything which is determined
// at parse/compile time inherited by the shell
// All components of ModuleContext are immutable and sharable at any one one
// time,
// but the static context itself is not imutable - it is affected by parse
// and runtime declarations
public class StaticContext implements Cloneable {
	private static Logger mLogger = LogManager.getLogger();
	private FunctionDefinitions mFunctions = null;
	private Modules mModules;                    // Imported modules visible to this module
	private int id = _id++;
	private static int _id = 0; 
	private		Namespaces	mNamespaces = null;
	
	
	// log debugging
	@Override
	public String toString() {
		return "CTX: " + id ;
	}
	
	public StaticContext()
	{
		mLogger.entry();
	}
	

	@Override
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
		if( that.mNamespaces != null )	
			mNamespaces = new Namespaces( that.mNamespaces );
		
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

	public FunctionDefinitions getFunctions() {

		if (mFunctions == null  )
			mFunctions = new FunctionDefinitions();
		return mFunctions;
	}

	public Modules getModules() {

		if( mModules == null  )
			mModules = new Modules();
		return mModules;
		
	}

	public Namespaces getNamespaces()
	{
	  if( mNamespaces == null )
      mNamespaces = new Namespaces();
		return mNamespaces;
	}

	public Iterable<IModule> getDefaultModules() {

		List<IModule> all = new ArrayList<>();
		for( IModule mh : mModules ){
			
			if( !  mModules.hasAnyPrefixes( mh )) 
				all.add(mh );
		}
		return mLogger.exit( all );
		
		
	}


}