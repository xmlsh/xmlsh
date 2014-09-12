package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.Collections;

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
public class ModuleContext {
	private static Logger mLogger = LogManager.getLogger();
	private FunctionDefinitions mFunctions = null;
	private Modules mModules;                    // Imported modules visible to this module
	private IModule mModule = null;              // The module this context is contained in  
	private int id = _id++;
	private static int _id = 0;
	
	private ModuleContext mParent ;				// The parent context if any - used when calling cross modules
	
	// log debugging
	public String toString() {
		return "CTX: " + id + ( mModule == null ? " <null> " : (" "  + mModule.toString() ));
	}
	
	public ModuleContext(IModule mod)
	{
		mLogger.entry(mod);

		assert( mod != null );
		mModule = mod ;
		mParent = null ;
	}
	

	public ModuleContext clone(Shell shell) throws IOException
	{

		mLogger.entry(this);
		return new ModuleContext( this );
	}

	
	// Clone this context 
	private ModuleContext(ModuleContext that) {
		mLogger.entry(that);
		assert( that.mModule != null );
		mModule = that.mModule ;
		
		if( that.mFunctions != null)
		    mFunctions = new FunctionDefinitions(that.mFunctions);
		
		if( that.mModules != null )
			mModules = new Modules( that.mModules);
		mParent = that.mParent;
		
	}
	public void declareFunction(IFunctionDecl func) {
		mLogger.entry(func);

		if (mFunctions == null)
			mFunctions = new FunctionDefinitions();
		mFunctions.put(func.getName(), func);
	}

	// Export out the current context, removing it from this one 
	// Used at the end of an import
	public ModuleContext exportContext() {
		mLogger.entry();
		ModuleContext ex = new ModuleContext( this );
		return mLogger.exit(ex);
		
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

	IModule getModule() {
		mLogger.entry();
		assert( mModule != null);
		return mLogger.exit(mModule);
	}

	private Modules getModules() {
		return mModules;
	}

	public Modules getModules(boolean bCreate) {
		if( mModules == null && bCreate )
			mModules = new Modules();
		return mModules;
		
	}

	public Iterable<IModule> getDefaultModules() {
		if( mModules == null )
			return Collections.emptyList();
		return getModules(true).getDefaultModules();
	}


	/*
	 * A cross module call 
	 * push the context of the called function/command and return the new context
	 */
	public ModuleContext pushContext(ModuleContext ctx) {
		assert( ctx != null );
		mLogger.entry( ctx );
		// Clone a context so changes dont propogate 
		ModuleContext newContext = new ModuleContext( ctx );
		newContext.mParent = this ;
		return mLogger.exit(newContext) ;
	}
	
	// Pop the context and detach it from its parent
	public ModuleContext popContext() 
	{
		mLogger.entry();
		assert( mParent != null );
		ModuleContext parent = mParent ;
		parent.mParent = null ;
		return parent;
		
	}


}