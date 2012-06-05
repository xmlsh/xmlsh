/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.util.Properties;


/**
 * This class replaces the System properties
 * to intercept calls to getProperty("user.dir") which is a thread local variable
 * 
 * @author David A. Lee
 */
@SuppressWarnings("serial")
class SystemProperties extends Properties {

	
	
	public SystemProperties() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SystemProperties(Properties defaults) {
		super(defaults);
		// TODO Auto-generated constructor stub
	}
	

	/* (non-Javadoc)
	 * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		
		if( key.equals("user.dir") ){
			String value = SystemEnvironment.getInstance().getProperty(key);
			if( value != null )
				return value ;
		
		}
		// cant use super.getProperty(key,def) as it recurses !
		String value = super.getProperty(key);
		if( value == null )
			value = defaultValue;
		return value;
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		
		if( key.equals("user.dir"))
			return getProperty(key,null);

		return super.getProperty(key);
	}

	
	
}

//
//
//Copyright (C) 2008-2012  David A. Lee.
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
