/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.io.File;
import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class chmod extends XCommand {
	
	private static class Perm
	{			
		boolean 	bRead = false;
		boolean		bWrite = false;
		boolean		bExecute = false ;


		Perm( File f ){
			bRead = f.canRead();
			bWrite = f.canWrite();
			bExecute = f.canExecute();
		}
		Perm( String s )
		{
			bRead = s.contains("r");
			bWrite = s.contains("w");
			bExecute = s.contains("x");
		}
		
		void add( Perm p )
		{
			bRead = bRead || p.bRead;
			bWrite = bWrite || p.bWrite ;
			bExecute = bExecute || p.bExecute;
		}
		void sub( Perm p )
		{
			if( p.bRead)
				bRead = false;
			if( p.bWrite )
				bWrite = false ;
			if( p.bExecute )
				bExecute = false ;
		}
	}
	
	private boolean bRecurse ;
	/*
	 * Parse a single permission mask [rxx]
	 */
	
	private void applyMode( String mode , File file ) throws InvalidArgumentException
	{
		for( String s : mode.split(",")){
			applyMode2(s,file);
		}
		if( bRecurse && file.isDirectory()){
			for( File child : file.listFiles()){
				applyMode( mode , child );
			}
		}
			
		
		
	}
	
	/*
	 * Parse a single mode and apply it to a file
	 *     [aou][-|+|=][rwx]
	 */
	private void applyMode2( String mode , File file ) throws InvalidArgumentException
	{
		
		boolean bOther = false ;
		boolean bOwner = false ;

		String[] fl = mode.split("[-+=]");
		if( fl.length != 2 )
			throw new InvalidArgumentException("Unexpected mode string: " + mode);
		
		String users = fl[0];
		String perms = fl[1];
		String plusminus = mode.substring(users.length() , mode.length() - perms.length() );
		
		Perm permFile = new Perm( file );
		Perm permAll = permFile ;
		Perm permUser = permFile ;
		Perm permMode = new Perm(perms);
		
		
		
		bOther = users.contains("o") || users.contains("a");
		bOwner = users.contains("u") || users.contains("a");	
		
		if( plusminus.equals("+")){
			if( bOther )
				permAll.add(permMode);
			if( bOwner )
				permUser.add(permMode);
			
		}
		else
		if( plusminus.equals("-")){
			if( bOther )
				permAll.sub(permMode);
			if( bOwner )
				permUser.sub(permMode);			
		}
		else
		if( plusminus.equals("="))
		{
			if( bOther )
				permAll = permMode;
			if( bOwner )
				permUser = permMode ;
		}
				
		/*
		 * other set - set all first
		 * owner set - the set owner only
		 * other but not owner - cant do
		 */
		

		if( bOther ){
			file.setReadable( permAll.bRead , false );
			file.setExecutable(permAll.bExecute , false);
			file.setWritable(permAll.bWrite , false );
		}
		if( bOwner ){
			file.setReadable( permUser.bRead , true);
			file.setExecutable(permUser.bExecute , true );
			file.setWritable(permUser.bWrite, true  );
		}
		
				
				
		
	}
	


	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		Options opts = new Options( "R=recurse" );
		opts.parse(args);
		args = opts.getRemainingArgs();
		bRecurse = opts.hasOpt("R");
		
		String mode = args.remove(0).toString();
		for( XValue arg : args){
			File f = this.getFile(arg);
			applyMode( mode , f );
			
		}
		
	
		
		return 0;
		
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
