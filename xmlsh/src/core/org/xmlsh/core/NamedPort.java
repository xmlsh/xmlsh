package org.xmlsh.core;



public class NamedPort<P extends IPort> {
	String		mName;		// Name
	P			mPort;		// port 
	boolean    mSystem;    // System port from original env
	
	public NamedPort( String name ,  P port   ) 
	{
		this(name,port,false);
	}
	
	public NamedPort( String name ,  P port , boolean system  ) 
	{
		mName = name ;
		mPort = port ;
		mSystem = system ;
	}
	
	public NamedPort( NamedPort<P> that ){
		mName = that.mName ;
		mPort = that.mPort;
		if( mPort != null )
			mPort.addRef();
	}
	public String getName() {
		return mName ;
	}
	public P getPort(){
		return mPort ;
	}

	public boolean getSystem()
	{
		return mSystem;
	}
	
	
}