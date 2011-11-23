/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.jmx;

import java.io.IOException;
import java.io.ObjectStreamField;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.jmx.util.JMXCommand;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.Util;

public class listen extends JMXCommand {

	private class  JMXListener implements NotificationListener 
	{

		@Override
		public void handleNotification(Notification notification, Object handback) {
			
			 try {
				mShell.getEnv().setStdin(new NullInputStream());
				mShell.setArg0("listen");
				List<XValue> args = new ArrayList<XValue>();
				args.add( new XValue( notification.getSource() ));			// $1 == source 
				args.add( new XValue( notification.getType()));				// $2 == type
				args.add( new XValue( notification.getTimeStamp() ));		// $3 == timestamp
				args.add( new XValue( notification.getSequenceNumber() )); 	// $4 == sequence#
				args.add( new XValue( notification.getMessage())); 			// $5 == message
				args.add( new XValue( notification.getUserData() ));		// $6 == userdata

				
				mShell.setArgs(args);
				mShell.exec(mCommand);
			} catch (Exception e) {
				
				mShell.printErr("Excepting running command" , e );
			}
			
			
			
			
		}
		
		
		
	}
	
	
	private		Command	mCommand;
	
	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(sCOMMON_OPTS,SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		args = opts.getRemainingArgs();
		
		if( args.size() != 2 ){
			usage();
			return 1;
		}
		

		
		ObjectName name = objectName(args.remove(0).toString());
		mCommand = mShell.parseEval(args.get(0).toString());
		
		
		JMXConnector jmx = getConnector(opts);
		try {
			MBeanServerConnection mbean = jmx.getMBeanServerConnection();
			NotificationListener listener = new JMXListener();
			
			mbean.addNotificationListener(name, listener, null, this);
	
			mShell.printOut("Listening ...");
			Thread.sleep(100000*10000);
		
		
		
		
		} finally {
			jmx.close();
		}
		return 0;
		
		
		
	}
}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
