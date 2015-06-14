/*
Wenity v1.5 - a Zenity clone written in Java

Copyright (c) 2012, 2013  Karoly Kalman  http://kksw.zzl.org/

This file is part of Wenity v1.5.

Wenity v1.5 is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wenity v1.5 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wenity v1.5.  If not, see <http://www.gnu.org/licenses/>.

*/


package wenity;

import java.util.Arrays;

public class Main
{

    public static void main (String[] args)
    {
        if (args.length == 0)
        {
            showUsage ();
            return;
        }

        // optional parameters
        if (Constants.PARAM_VERBOSE.equals (args[0]))
        {
            Logger.goDebugMode ();
        }
//        else
//            if (Constants.PARAM_INFO.equals (args[0]))
//            {
//                Logger.goInfoMode ();
//            }

        Logger.debug ("Wenity called with arguments: " + Arrays.toString (args));

        System.exit (new Wenity ().doIt (args));
    }


    private static void showUsage ()
    {
        System.out.println (Constants.APP_VERSION);
        System.out.println ("Usage: [-d] module_name module_parameters");
        System.out.println ("See documentation for details and examples or visit Wenity on internet at " + Constants.APP_HOME);
    }
}