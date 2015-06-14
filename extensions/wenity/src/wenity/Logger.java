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

public final class Logger
{
    private static boolean _DEBUG = false;
    private static boolean _INFO = false;

    private Logger ()
    {
    }

    public static void goDebugMode ()
    {
        Logger._DEBUG = true;
        Logger._INFO = true;
    }

    public static void goInfoMode ()
    {
        Logger._INFO = true;
    }

//    public static final void info (final String message)
//    {
//        if (_INFO)
//            System.out.println (message);
//    }

    public static final void debug (final String message)
    {
        if (_DEBUG)
            System.out.println (message);
    }

    public static final void debug (final String message, final Object... msgArgs)
    {
        if (_DEBUG)
            System.out.println (String.format (message, msgArgs));
    }

    public static final void error (final String message, final Object... msgArgs)
    {
        System.err.println (String.format (message, msgArgs));
    }

    public static final void exception (final String message, final Exception ex)
    {
        System.err.println (message + "\n-------------\n" + ex+"\n-------------\n");
//        ex.printStackTrace ();
    }


    public static boolean debugOn ()
    {
        return _DEBUG;
    }

}
