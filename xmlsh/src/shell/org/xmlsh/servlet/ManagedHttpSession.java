/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.servlet;

import org.xmlsh.util.ManagedObject;

import javax.servlet.http.HttpSession;

public class ManagedHttpSession extends ManagedObject
{
	private		HttpSession 	mSession ;


	public ManagedHttpSession(HttpSession session) {
		super();
		mSession = session;
	}


	@Override
	public void close() {
		mSession = null ;

	}


	/**
	 * @return the session
	 */
	public HttpSession getSession() {
		return mSession;
	}



}


//
//
//Copyright (C) 2008-2014 David A. Lee.
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
