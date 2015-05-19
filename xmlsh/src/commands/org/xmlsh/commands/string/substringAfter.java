/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.string;

import java.util.List;

import net.sf.saxon.trans.XPathException;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class substringAfter extends BuiltinFunctionCommand {

    public substringAfter()
    {
        super("substring-after");
    }

    // string:substring-after( string [sub] )
    // using XQuery rules - 
    // if sub is empty then return string 
    @Override
    public XValue run(Shell shell, List<XValue> args) throws UnexpectedException, XPathException,
            InvalidArgumentException {
        switch( args.size()){
        case 0: 
            return new XValue( "" );
        case 1:
            return args.get(0);
        case 2:
            String s = args.get(0).toString();
            String sub = args.get(1).toString();
            if( ! sub.isEmpty() ){
                int pos = s.lastIndexOf(sub);
                if( pos < 0 ) // not found
                    s = "";
                else
                if(  pos != s.length() )
                    s = s.substring( pos + sub.length() );
            }
            return new XValue(s);
        default :
            throw new InvalidArgumentException("usage: substring-after( string substr)");
        }
    }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
