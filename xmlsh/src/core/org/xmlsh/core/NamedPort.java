package org.xmlsh.core;



public class NamedPort<P extends IPort> {
	String		mName;		// Name
	P			mPort;		// port 
	
	public NamedPort( String name ,  P port  )
	{
		mName = name ;

		mPort = port ;
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
}