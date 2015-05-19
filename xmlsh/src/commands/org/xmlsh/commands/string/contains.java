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

public class contains extends BuiltinFunctionCommand {

    public contains()
    {
        super("contains");
    }

    // string:join( sep , arg ... )
    @Override
    public XValue run(Shell shell, List<XValue> args) throws UnexpectedException, XPathException,
            InvalidArgumentException {

        switch( args.size() )
        {
        case 1:
            return new XValue(false);
        case 2:
            String str = args.get(0).toString();
            String search = args.get(1).toString();
            return new XValue(str.contains(search));    
        default :
            throw new InvalidArgumentException("usage  contains(string expr)");
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
